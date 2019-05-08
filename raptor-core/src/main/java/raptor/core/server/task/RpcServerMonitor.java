package raptor.core.server.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;

/**
 * @author gewx RPC Server监视器扫描服务
 * 
 **/
public final class RpcServerMonitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerMonitor.class);

	private static final ScheduledExecutorFactoryBean factory = new ScheduledExecutorFactoryBean();

	private RpcServerMonitor() {
	}

	/**
	 * 扫描超时请求.
	 **/
	public static void scan() {
		LOGGER.info("RpcServerMonitor监视器扫描...");
		ScheduledExecutorTask task = new ScheduledExecutorTask();
		/**
		 * 在第一次(首次)启动任务之前设置延迟，以毫秒为单位。默认值为0，成功调度后立即启动任务。
		 **/
		 //task.setDelay(1000 * 30);

		/**
		 * 设置是否调度为固定速率执行，而不是固定延迟执行。默认值是“false”，即固定延迟。
		 **/
		task.setFixedRate(false);

		/**
		 * 以毫秒为单位设置重复执行任务之间的周期。
		 **/
		task.setPeriod(1000 * 30); // 任务间隔100毫秒.

		task.setRunnable(new Runnable() {
			 	@Override
				public void run() {
			 		/*
					 Map<String, Channel> queue = RpcMonitorQueue.SERVER_QUEUE;
					 for (Map.Entry<String, Channel> en : queue.entrySet()) {
						 System.out.println("扫描服务目标IP主机: " + en.getKey());
						 RpcResponseBody rpcBody = new RpcResponseBody();
						 rpcBody.setBody("测试服务器主动推送熔断标记!");
						 rpcBody.setRpcMethod("RT");
						 en.getValue().writeAndFlush(rpcBody);
					 }
					 */
				}
		});

		factory.setScheduledExecutorTasks(task);
		factory.setContinueScheduledExecutionAfterException(true); // 调度遇到异常后,调度计划继续执行.
		factory.setThreadNamePrefix("TASK_RPC_MONITOR_SCAN_");
		factory.initialize(); 
	}
}
