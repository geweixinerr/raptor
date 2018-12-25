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

/**
 * @author gewx RPC请求客户端超时扫描: 本线程执行过期消息清理.
 **/
public final class RpcClientTimeOutScan {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientTimeOutScan.class);

	private static final ScheduledExecutorFactoryBean factory = new ScheduledExecutorFactoryBean();

	private RpcClientTimeOutScan() {
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
		 * 以毫秒为单位设置重复执行任务之间的周期。
		 **/
		task.setPeriod(200); // 任务间隔200毫秒.

		task.setRunnable(new Runnable() {
			 	@Override
				public void run() {
					Map<String, RpcRequestBody> requestPool = RpcClientTaskPool.listMapPool(); //客户端请求-回调任务池.
					
					for (Map.Entry<String, RpcRequestBody> en : requestPool.entrySet()) {
						RpcRequestBody requestBody = en.getValue(); 
						// 如果当前时间已超过设置的超时时间,则为过期消息.
						DateTime thisDate = new DateTime();
						if (thisDate.compareTo(requestBody.getTimeOut()) >= 0) {
							String messageId = requestBody.getMessageId();
							requestPool.remove(messageId); // delete,mark:此处删除已回调超时信息,避免客户端回调重复执行此信息.
							
							if (!requestBody.isMessageSend()) { // 消息未发送
								RpcResponseBody responseBody = new RpcResponseBody();
								responseBody.setSuccess(false);
								responseBody.setMessageId(requestBody.getMessageId());
								responseBody.setMessage("RPC 服务调用失败,message:timeOut");
								responseBody.setRpcCode(RpcResult.TIME_OUT);
								requestBody.setResponseTime(thisDate);
						
								requestBody.getCall().invoke(responseBody); // 回调通知
								LOGGER.warn("清理超时消息...,messageId: " + requestBody);
							}
						}				
					}
				}
		});

		factory.setScheduledExecutorTasks(task);
		factory.setContinueScheduledExecutionAfterException(true); // 调度遇到异常后,调度计划继续执行.
		factory.setThreadNamePrefix("TASK_RPC_CLIENT_SCAN_");
		factory.initialize(); // 初始化
	}
}
