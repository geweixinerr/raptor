package raptor.core.server.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;

/**
 * RPC Server监视器扫描服务
 * 
 * @author gewx 
 * 
 **/
public final class RpcServerMonitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerMonitor.class);

	private static final ScheduledExecutorFactoryBean SCHEDULED_FACTORY = new ScheduledExecutorFactoryBean();

	private RpcServerMonitor() {
	}

	/**
	 * 扫描超时请求
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
		 * 以毫秒为单位设置重复执行任务之间的周期, 任务间隔100毫秒.。
		 **/
		task.setPeriod(1000 * 30); 

		task.setRunnable(() -> {
			
		});

		SCHEDULED_FACTORY.setScheduledExecutorTasks(task);
		// 调度遇到异常后,调度计划继续执行.
		SCHEDULED_FACTORY.setContinueScheduledExecutionAfterException(true); 
		SCHEDULED_FACTORY.setThreadNamePrefix("TASK_RPC_MONITOR_SCAN_");
		SCHEDULED_FACTORY.initialize(); 
	}
}
