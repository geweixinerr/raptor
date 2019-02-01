package raptor.core.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.joda.time.DateTime;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import raptor.core.Constants;
import raptor.core.RpcResult;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.log.RaptorLogger;

/**
 * @author gewx RPC Client端业务线程池
 **/

public final class RpcClientTaskPool {

	private static final RaptorLogger LOGGER = new RaptorLogger(RpcClientTaskPool.class);

	private static final ThreadPoolTaskExecutor POOLTASKEXECUTOR = new ThreadPoolTaskExecutor();

	private static final Map<String, RpcRequestBody> MESSAGEID_MAPPING = new ConcurrentHashMap<String, RpcRequestBody>(1024 * 10);
		
	private RpcClientTaskPool() {
	}

	/**
	 * @author gewx 初始化线程配置
	 * 
	 **/
	public static void initPool() {
		LOGGER.info("初始化RPC Client业务线程池对象...");
		POOLTASKEXECUTOR.setQueueCapacity(Constants.CPU_CORE * 10240); // 队列深度.
		POOLTASKEXECUTOR.setCorePoolSize(Constants.CPU_CORE); // 核心线程数.
		POOLTASKEXECUTOR.setMaxPoolSize(Constants.CPU_CORE * 4); // 最大线程数.
		POOLTASKEXECUTOR.setKeepAliveSeconds(60 * 5); //线程最大空闲时间5分钟可回收,默认60秒.
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_CLIENT_"); // 线程名前缀.
		POOLTASKEXECUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); //discard
		POOLTASKEXECUTOR.initialize();

		// 启动全部核心线程,避免线程在服务运行期间NEW,继而导致系统吞吐能力抖动.
		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}

	/**
	 * @author gewx 添加客户端请求回调任务入业务线程池
	 * @param responseBody
	 *            请求参数
	 * @return void
	 **/
	public static void addTask(RpcResponseBody responseBody) {
		POOLTASKEXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				RaptorLogger.THREAD_ID.set(responseBody.getThreadId());

				LOGGER.info("RPC客户端收到响应: " + responseBody);
				
				RpcRequestBody requestBody = MESSAGEID_MAPPING.remove(responseBody.getMessageId());
				if (requestBody == null) {
					return;
				}

				DateTime thisDate = new DateTime();
				requestBody.setClientTime(responseBody.getResponseTime()); 
				requestBody.setResponseTime(thisDate);
				
				//sync
				if (requestBody.isSync()) {
					requestBody.getCall().invoke(responseBody);
					return;
				}
				
				//async				
				if (thisDate.compareTo(requestBody.getTimeOut()) <= 0) {
					if (!requestBody.isMessageSend()) { //是否已发送,并发串行化校验.
						requestBody.getCall().invoke(responseBody);
					}
				} else {
					//重写响应response对象[此刻无论响应处理是否成功,但凡客户端调用超时,则认为业务端调用失败!].
					responseBody.setMessage("RPC 服务调用超时,message:timeOut");
					responseBody.setRpcCode(RpcResult.TIME_OUT);
					requestBody.getCall().invoke(responseBody);
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
