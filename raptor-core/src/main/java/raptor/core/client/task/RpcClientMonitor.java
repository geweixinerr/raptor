package raptor.core.client.task;

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;

import raptor.core.RpcResult;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.util.StringUtil;

/**
 * @author gewx RPC 客户端监视器,定时执行超时扫描: 本线程执行过期消息清理.
 * 
 **/
public final class RpcClientMonitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientMonitor.class);

	private static final ScheduledExecutorFactoryBean SCHEDULED_FACTORY = new ScheduledExecutorFactoryBean();

	private RpcClientMonitor() {
	}

	/**
	 * 扫描超时请求.
	 **/
	public static void scan() {
		LOGGER.info("客户端超时消息清理器启动...");
		ScheduledExecutorTask task = new ScheduledExecutorTask();
		/**
		 * 在第一次(首次)启动任务之前设置延迟，以毫秒为单位。默认值为0，成功调度后立即启动任务。
		 **/
		// task.setDelay(0);

		/**
		 * 设置是否调度为固定速率执行，而不是固定延迟执行。默认值是“false”，即固定延迟。
		 **/
		task.setFixedRate(false);

		/**
		 * 以毫秒为单位设置重复执行任务之间的周期,任务间隔100毫秒.。
		 **/
		task.setPeriod(100); 

		task.setRunnable(() -> {
			Map<String, RpcRequestBody> requestPool = RpcClientTaskPool.listMapPool();
			// 客户端请求-回调任务池.
			String[] keys = requestPool.keySet().toArray(new String[] {}); 

			for (String key : keys) {
				RpcRequestBody requestBody = requestPool.get(key);
				if (requestBody == null || requestBody.isSync()) {
					continue;
				}
				// 如果当前时间已超过设置的超时时间,则为过期消息.
				DateTime thisDate = new DateTime();
				if (thisDate.compareTo(requestBody.getTimeOut()) >= 0) {
					String messageId = requestBody.getMessageId();
					requestPool.remove(messageId);

					if (!requestBody.isMessageSend()) {
						Integer rpcTime = StringUtil.timeDiffForMilliSecond(requestBody.getRequestTime(), thisDate);
						RpcResponseBody responseBody = new RpcResponseBody();
						responseBody.setMessageId(requestBody.getMessageId());
						responseBody.setMessage("RPC 服务调用超时,message:timeOut");
						responseBody.setRpcCode(RpcResult.SCAN_TIME_OUT);
						responseBody.setRpcTime(rpcTime);

						// 回调通知
						requestBody.getCall().invoke(responseBody); 
					}
				}
			}
		});

		SCHEDULED_FACTORY.setScheduledExecutorTasks(task);
		// 调度遇到异常后,调度计划继续执行.
		SCHEDULED_FACTORY.setContinueScheduledExecutionAfterException(true); 
		SCHEDULED_FACTORY.setThreadNamePrefix("TASK_RPC_CLIENT_SCAN_");
		SCHEDULED_FACTORY.initialize();
	}
}
