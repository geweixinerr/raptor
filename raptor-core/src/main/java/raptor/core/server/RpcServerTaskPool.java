package raptor.core.server;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.alibaba.ttl.TtlCallable;

import raptor.core.AbstractCallBack;
import raptor.core.Constants;
import raptor.core.RpcResult;
import raptor.core.init.RpcHandlerObject;
import raptor.core.init.RpcMapping;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;
import raptor.util.StringUtil;

/**
 * @author gewx RPC Server端业务线程池
 **/

public final class RpcServerTaskPool {

	private static final RaptorLogger LOGGER = new RaptorLogger(RpcServerTaskPool.class);

	private static final ThreadPoolTaskExecutor POOLTASKEXECUTOR = new ThreadPoolTaskExecutor();

	private static final Map<String, RpcHandlerObject> RPC_MAPPING = RpcMapping.listRpcMapping();

	private RpcServerTaskPool() {
	}

	/**
	 * 初始化线程配置[根据各自系统实际情况,可自行定制]
	 * 
	 * @author gewx
	 * @return void
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Server业务线程池对象...");
		// 队列深度.
		POOLTASKEXECUTOR.setQueueCapacity(Constants.CPU_CORE * 10240);
		// 核心线程数.
		POOLTASKEXECUTOR.setCorePoolSize(Constants.CPU_CORE);
		// 最大线程数.
		POOLTASKEXECUTOR.setMaxPoolSize(Constants.CPU_CORE * 4);
		// 线程最大空闲时间30分钟可回收,默认60秒.
		POOLTASKEXECUTOR.setKeepAliveSeconds(60 * 30);
		// 线程名前缀.
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_SERVER_");
		// discard
		POOLTASKEXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
		POOLTASKEXECUTOR.initialize();

		// 启动全部核心线程,避免线程在服务运行期间NEW,继而导致系统吞吐能力抖动.
		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}

	/**
	 * 添加任务入业务线程池
	 * 
	 * @author gewx
	 * @param requestBody 请求对象, call 业务回调对象.
	 * @return void
	 **/
	public static void addTask(RpcRequestBody requestBody, AbstractCallBack call) {
		String rpcMethod = requestBody.getRpcMethod();
		String traceId = requestBody.getTraceId();
		ListenableFuture<RpcResponseBody> future = POOLTASKEXECUTOR
				.submitListenable(TtlCallable.get(new Callable<RpcResponseBody>() {
					@Override
					public RpcResponseBody call() throws Exception {
						LOGGER.info("RPC服务端收到请求信息: " + requestBody);

						RpcHandlerObject handler = RPC_MAPPING.get(rpcMethod);
						if (handler == null) {
							throw new RpcException("RPC参数缺失,RpcMethod is null !", RpcResult.ERROR);
						}

						Object result = null;
						Object[] objArray = requestBody.getBody();
						if (ArrayUtils.isNotEmpty(objArray)) {
							// MethodUtils.invokeExactMethod(object, methodName); 根据类型完全匹配.
							result = MethodUtils.invokeMethod(handler.getObject(), handler.getRpcKey(), objArray);
						} else {
							result = MethodUtils.invokeMethod(handler.getObject(), handler.getRpcKey());
						}

						RpcResponseBody body = new RpcResponseBody();
						body.setRpcCode(RpcResult.SUCCESS);
						body.setMessageId(requestBody.getMessageId());
						body.setTraceId(traceId);
						body.setRpcMethod(rpcMethod);
						body.setBody(result);
						body.setMessage("RPC调用成功!");
						return body;
					}
				}));

		future.addCallback(new ListenableFutureCallback<RpcResponseBody>() {

			@Override
			public void onSuccess(RpcResponseBody body) {
				call.invoke(body);
			}

			@Override
			public void onFailure(Throwable throwable) {
				String message = "RPC 服务调用失败,message:[" + StringUtil.getErrorText(throwable) + "]";
				LOGGER.warn(rpcMethod, message);

				/**
				 * 定义回调异常,默认响应体
				 **/
				RpcResponseBody body = new RpcResponseBody();
				body.setRpcCode(RpcResult.FAIL);
				body.setMessageId(requestBody.getMessageId());
				body.setTraceId(traceId);
				body.setRpcMethod(rpcMethod);
				body.setMessage("RPC 服务调用失败,message:[" + ExceptionUtils.getRootCauseMessage(throwable) + "]");

				call.invoke(body);
			}
		});
	}
}
