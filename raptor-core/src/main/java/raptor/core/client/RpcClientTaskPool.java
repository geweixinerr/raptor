package raptor.core.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import raptor.core.AbstractCallBack;
import raptor.core.init.RpcHandlerObject;
import raptor.core.init.RpcMappingInit;
import raptor.core.message.RpcRequestBody;

/**
 * @author gewx RPC Client端业务线程池
 **/

public final class RpcClientTaskPool {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientTaskPool.class);

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
	
	private RpcClientTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Client业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(CPU_CORE * 10000);
		POOLTASKEXECUTOR.setCorePoolSize(CPU_CORE); // 核心线程数
		POOLTASKEXECUTOR.setMaxPoolSize(CPU_CORE * 3); // 最大线程数
		// poolTaskExecutor.setKeepAliveSeconds(5000); //线程最大空闲时间-可回收
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_CLIENT_"); // 线程名前缀.
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
 
	}
}
