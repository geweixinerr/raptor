package raptor.core.server;

import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import raptor.core.AbstractCallBack;
import raptor.core.init.RpcHandlerObject;
import raptor.core.init.RpcMappingInit;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;

/**
 * @author gewx RPC Server端业务线程池
 **/

public final class RpcServerTaskPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerTaskPool.class);

	// CPU核心数
	private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

	/**
	 * 线程池对象
	 **/
	private static final ThreadPoolTaskExecutor POOLTASKEXECUTOR = new ThreadPoolTaskExecutor();

	/**
	 * RPC映射关系
	 * **/
	private static final Map<String,RpcHandlerObject> RPC_MAPPING = RpcMappingInit.listRpcMapping();
	
	private RpcServerTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Server业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(CPU_CORE * 10000);
		POOLTASKEXECUTOR.setCorePoolSize(CPU_CORE); // 核心线程数
		POOLTASKEXECUTOR.setMaxPoolSize(CPU_CORE * 3); // 最大线程数
		// poolTaskExecutor.setKeepAliveSeconds(5000); //线程最大空闲时间-可回收
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_SERVER_"); // 线程名前缀.
		POOLTASKEXECUTOR.initialize();

		// 启动全部核心线程.
		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}

	/**
	 * @author gewx 添加任务入业务线程池
	 * @param Object
	 *            obj 请求参数, AbstractCallBack call 业务回调对象.
	 * @return void
	 **/
	public static void addTask(RpcRequestBody requestBody, AbstractCallBack call) {
		LOGGER.info("RPC请求任务入池,MessageId: " + requestBody.getMessageId());
		ListenableFuture<RpcResponseBody> future = POOLTASKEXECUTOR
				.submitListenable(new Callable<RpcResponseBody>() {
					@Override
					public RpcResponseBody call() throws Exception {
						/**
						 * 实际业务调用
						 * **/
						RpcHandlerObject handler = RPC_MAPPING.get(requestBody.getRpcMethod());
						if (handler == null) {
							throw new RpcException("RPC参数缺失,RpcMethod is null !");
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
						body.setBody(result);
						body.setMessageId(requestBody.getMessageId());
						body.setSuccess(true);
						body.setMessage("RPC调用成功!");
						return body;
					}
				});
		
		future.addCallback(new ListenableFutureCallback<RpcResponseBody>() {
			final RpcResult result = new RpcResult();

			@Override
			public void onSuccess(RpcResponseBody body) {
				result.setSuccess(true);
				result.setResponseBody(body);
				call.invoke(result);
			}

			@Override
			public void onFailure(Throwable throwable) {
				/**
				 * 定义回调异常,默认响应体
				 * **/
				RpcResponseBody body = new RpcResponseBody(); 
				body.setSuccess(false);
				body.setMessageId(requestBody.getMessageId());
				body.setMessage("RPC 服务调用失败,message:[" + throwable.getMessage() + "]");
				
				//Result
				result.setSuccess(false);
				result.setResponseBody(body);
				result.setThrowable(throwable);
				call.invoke(result);
			}
		});
	}
}
