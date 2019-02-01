package raptor.core.server;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import raptor.core.AbstractCallBack;
import raptor.core.Constants;
import raptor.core.RpcResult;
import raptor.core.init.RpcHandlerObject;
import raptor.core.init.RpcMappingInit;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;

/**
 * @author gewx RPC Server端业务线程池
 **/

public final class RpcServerTaskPool {

	private static final RaptorLogger LOGGER = new RaptorLogger(RpcServerTaskPool.class);
	
	private static final Logger RAW_LOGGER = LoggerFactory.getLogger(RpcServerTaskPool.class);

	private static final ThreadPoolTaskExecutor POOLTASKEXECUTOR = new ThreadPoolTaskExecutor();

	private static final Map<String,RpcHandlerObject> RPC_MAPPING = RpcMappingInit.listRpcMapping();
	
	private RpcServerTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		RAW_LOGGER.info("初始化RPC Server业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(Constants.CPU_CORE * 10240); //队列深度. 
		POOLTASKEXECUTOR.setCorePoolSize(Constants.CPU_CORE); // 核心线程数. 
		POOLTASKEXECUTOR.setMaxPoolSize(Constants.CPU_CORE * 4); // 最大线程数. 
		POOLTASKEXECUTOR.setKeepAliveSeconds(60 * 5); //线程最大空闲时间5分钟可回收,默认60秒.
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_SERVER_"); // 线程名前缀.
		POOLTASKEXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); //discard
		POOLTASKEXECUTOR.initialize();
		
		// 启动全部核心线程,避免线程在服务运行期间NEW,继而导致系统吞吐能力抖动.
		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}

	/**
	 * @author gewx 添加任务入业务线程池
	 * @param Object
	 *            obj 请求参数, AbstractCallBack call 业务回调对象.
	 * @return void
	 **/
	public static void addTask(RpcRequestBody requestBody, AbstractCallBack call) {
	    String rpcMethod = requestBody.getRpcMethod();
	    String threadId = requestBody.getThreadId();
		ListenableFuture<RpcResponseBody> future = POOLTASKEXECUTOR
				.submitListenable(new Callable<RpcResponseBody>() {
					@Override
					public RpcResponseBody call() throws Exception {
						RaptorLogger.THREAD_ID.set(requestBody.getThreadId());

						LOGGER.info("RPC服务端收到请求信息: " + requestBody);		
						
						RpcHandlerObject handler = RPC_MAPPING.get(rpcMethod);
						if (handler == null) {
							throw new RpcException("RPC参数缺失,RpcMethod is null !", RpcResult.ERROR);
						}
						
						Object result = null;
						Object [] objArray = requestBody.getBody();
						if (ArrayUtils.isNotEmpty(objArray)) {
							         //MethodUtils.invokeExactMethod(object, methodName); 根据类型完全匹配.
							result = MethodUtils.invokeMethod(handler.getObject(), handler.getRpcKey(), objArray);
						} else {
							result = MethodUtils.invokeMethod(handler.getObject(), handler.getRpcKey());
						}
						RpcResponseBody body = new RpcResponseBody();
						body.setRpcCode(RpcResult.SUCCESS);
						body.setMessageId(requestBody.getMessageId());
						body.setThreadId(threadId);
						body.setRpcMethod(rpcMethod);
						body.setBody(result);
						body.setMessage("RPC调用成功!");
						return body;
					}
				});

		future.addCallback(new ListenableFutureCallback<RpcResponseBody>() {

			@Override
			public void onSuccess(RpcResponseBody body) {
				call.invoke(body);
			}

			@Override
			public void onFailure(Throwable throwable) {
				String message = "RPC 服务调用失败,message:[" + ExceptionUtils.getRootCauseMessage(throwable) + "]";
				LOGGER.warn(rpcMethod, message);	

				/**
				 * 定义回调异常,默认响应体
				 * **/
				RpcResponseBody body = new RpcResponseBody();
				body.setRpcCode(RpcResult.FAIL);
				body.setMessageId(requestBody.getMessageId());
				body.setThreadId(threadId);
				body.setRpcMethod(rpcMethod);
				body.setMessage(message);
				
				call.invoke(body);
			}
		});
	}
}
