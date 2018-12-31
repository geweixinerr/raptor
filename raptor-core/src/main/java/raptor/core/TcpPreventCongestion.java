package raptor.core;

import java.util.concurrent.TimeUnit;

/**
 * @author gewx Tcp 防拥塞.[高并发下大量的tcp小包容易堵塞传输网络,适当降低传输速率提高响应性]
 **/
public final class TcpPreventCongestion {
	
	/**
	 * @author gewx 简单堵塞
	 **/
	public synchronized void congestion(final int sleepTime) {
		/*
		 * TimeUnit.DAYS //天 TimeUnit.HOURS //小时 TimeUnit.MINUTES //分钟 TimeUnit.SECONDS
		 * //秒 TimeUnit.MILLISECONDS //毫秒 TimeUnit.NANOSECONDS //毫微秒
		 */

		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}

}
