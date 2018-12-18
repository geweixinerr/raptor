package raptor.core.client;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
	private static final Map<String, RpcRequestBody> MESSAGEID_MAPPING = new ConcurrentHashMap<>(1024 * 10);

	/**
	 * 一个基于链接节点的无界线程安全队列。此队列按照 FIFO（先进先出）原则对元素进行排序
	 **/
	private static final ConcurrentLinkedQueue<RpcRequestBody> CLIENT_POOL_QUEUE = new ConcurrentLinkedQueue<RpcRequestBody>();

	private RpcClientTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Client业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(CPU_CORE * 1024); // 队列深度
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
	 * @param responseBody
	 *            请求参数
	 * @return void
	 **/
	public static void addTask(RpcResponseBody responseBody) {
		LOGGER.info("RPC调用响应:" + responseBody);
		POOLTASKEXECUTOR.submitListenable(new Callable<RpcResponseBody>() {
			@Override
			public RpcResponseBody call() throws Exception {
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
						System.out.println("客户端-服务器执行耗时: " + requestBody);
						LOGGER.info("成功执行回调,messageId: " + responseBody.getMessageId());
					}
				} else {
					// 此处消息超时.
				}
				
				return null;
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
		CLIENT_POOL_QUEUE.offer(requestBody); // 入队列.
	}

	/**
	 * @author gew 获取客户端请求队列池
	 * @param void
	 * @return 客户端池
	 **/
	public static Map<String, RpcRequestBody> listMapPool() {
		return MESSAGEID_MAPPING;
	}

	/**
	 * @author gewx 获取Queue队列
	 * @param void
	 * @return ConcurrentLinkedQueue Object
	 * 
	 **/
	public static ConcurrentLinkedQueue<RpcRequestBody> listQueuePool() {
		return CLIENT_POOL_QUEUE;
	}
}
