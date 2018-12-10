package raptor.core.server;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import raptor.core.AbstractCallBack;

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

	private RpcServerTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Server业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(10000);
		POOLTASKEXECUTOR.setCorePoolSize(CPU_CORE); // 核心线程数
		POOLTASKEXECUTOR.setMaxPoolSize(CPU_CORE * 3); // 最大线程数
		// poolTaskExecutor.setKeepAliveSeconds(5000); //线程最大空闲时间-可回收
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_SERVER"); // 线程名前缀.
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
	public static void addTask(Object obj, AbstractCallBack call) {
		LOGGER.info("RPC请求任务入池,MessageId: " + null);
		ListenableFuture<Map<String, Object>> future = POOLTASKEXECUTOR
				.submitListenable(new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() throws Exception {
						// 执行反射,业务调用等!
						return null;
					}
				});

		future.addCallback(new ListenableFutureCallback<Map<String, Object>>() {
			final RpcResult result = new RpcResult();

			@Override
			public void onSuccess(Map<String, Object> arg0) {
				result.setSuccess(true);
				result.setMessage("RPC 调用成功!");
				call.invoke(result);
			}

			@Override
			public void onFailure(Throwable throwable) {
				result.setSuccess(false);
				result.setMessage("RPC 服务调用失败,message:[" + throwable.getMessage() + "]");
				result.setThrowable(throwable);
				call.invoke(result);
			}
		});
	}
}
