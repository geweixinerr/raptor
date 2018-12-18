package raptor.core.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;

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
	 * 客户端请求MessageId与回调对应关系.
	 * **/
	private static final Map<String,RpcRequestBody> MESSAGEID_MAPPING = new ConcurrentHashMap<>(1024 * 10); 
	
	private RpcClientTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Client业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(CPU_CORE * 1024); //队列深度
		POOLTASKEXECUTOR.setCorePoolSize(CPU_CORE); // 核心线程数
		POOLTASKEXECUTOR.setMaxPoolSize(CPU_CORE * 3); // 最大线程数
		// poolTaskExecutor.setKeepAliveSeconds(5000); //线程最大空闲时间-可回收
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_CLIENT_"); // 线程名前缀.
		POOLTASKEXECUTOR.initialize();

		// 启动全部核心线程.
		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}

	/**
	 * @author gewx 添加客户端请求回调任务入业务线程池
	 * @param responseBody 请求参数
	 * @return void
	 **/
	public static void addTask(RpcResponseBody responseBody) {
		LOGGER.info("RPC调用响应:" + responseBody);
				
		RpcRequestBody requestBody = MESSAGEID_MAPPING.remove(responseBody.getMessageId());
		if (requestBody != null && !requestBody.isMessageSend()) {
			requestBody.getCall().invoke(responseBody);
			LOGGER.info("成功执行回调,messageId: " + responseBody.getMessageId());
		} else {
			//LOGGER.info("RPC回调超时,messageId: " + responseBody.getMessageId());
		}
	}
	
	/**
	 * @author gewx 
	 * @param requestBody 客户端请求入队列
	 * **/
	public static void pushTask(RpcRequestBody requestBody) {
		MESSAGEID_MAPPING.put(requestBody.getMessageId(), requestBody);
	}
	
	/**
	 * @author gew 获取客户端请求队列池
	 * @param void
	 * @return 客户端池
	 * **/
	public static Map<String,RpcRequestBody> getClientTaskPool() {
		return MESSAGEID_MAPPING;
	}
}
