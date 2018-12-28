package raptor.core;

import java.util.concurrent.TimeUnit;

/**
 * @author gewx Tcp 防拥塞.[高并发下大量的tcp小包容易堵塞传输网络,适当降低传输速率提高响应性]
 * **/
public final class TcpPreventCongestion {

	private TcpPreventCongestion() {}
	
	public static final TcpPreventCongestion INSTANCE = new TcpPreventCongestion();
	
	/**
	 * @author gewx 简单堵塞
	 * **/
	public synchronized void congestion(final int sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}
	
}
