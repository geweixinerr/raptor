package raptor.core.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
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
	 **/
	private static final Map<String, RpcRequestBody> MESSAGEID_MAPPING = new ConcurrentHashMap<String, RpcRequestBody>(1024 * 10);
	
	private RpcClientTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Client业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(CPU_CORE * 1024 * 100); // 队列深度
		POOLTASKEXECUTOR.setCorePoolSize(CPU_CORE); // 核心线程数
		POOLTASKEXECUTOR.setMaxPoolSize(CPU_CORE * 8); // 最大线程数
		POOLTASKEXECUTOR.setKeepAliveSeconds(5000); //线程最/大空闲时间-可回收
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_CLIENT_"); // 线程名前缀.
		
		POOLTASKEXECUTOR.initialize();

		// 启动全部核心线程.
		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}

	/**
	 * @author gewx 添加客户端请求回调任务入业务线程池
	 * @param responseBody
	 *            请求参数
	 * @return void
	 **/
	public static void addTask(RpcResponseBody responseBody) {
		//LOGGER.info("RPC调用响应:" + responseBody);
		POOLTASKEXECUTOR.execute(new Runnable() {
			
			@Override
			public void run() {
				/**
				 * 实际客户端回调处理
				 **/
				RpcRequestBody requestBody = MESSAGEID_MAPPING.remove(responseBody.getMessageId());

				/**
				 * 此处逻辑描述: 1.客户端任务池存在未超时的回调任务,判断当前任务时间是否超时。
				 * 2.未超时则判断当前消息是否已发送(消息对象(RpcRequestBody)存在并发调用,串行化控制消息的发送)。 3.清理队列中已发送的消息对象。
				 * 
				 **/
				if (requestBody != null && new DateTime().compareTo(requestBody.getTimeOut()) <= 0) {
					if (!requestBody.isMessageSend()) {
						requestBody.setResponseTime(new DateTime()); // 客户端回调时间
						requestBody.getCall().invoke(responseBody);
						LOGGER.warn("成功执行回调,messageId: " + requestBody);
					}
				} else {
					// 此处消息超时,或者消息未成功到达服务器(异常),并已响应客户端.
				}
			}
		});
	}

	/**
	 * @author gewx
	 * @param requestBody
	 *            客户端请求入队列
	 **/
	public static void pushTask(RpcRequestBody requestBody) {
		MESSAGEID_MAPPING.put(requestBody.getMessageId(), requestBody);
	}

	/**
	 * @author gew 获取客户端请求队列池
	 * @param void
	 * @return 客户端池
	 **/
	public static Map<String, RpcRequestBody> listMapPool() {
		return MESSAGEID_MAPPING;
	}
}
