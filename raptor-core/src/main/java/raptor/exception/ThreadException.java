package raptor.exception;

import raptor.log.RaptorLogger;
import raptor.util.StringUtil;


/**
 * @author gewx 线程异常处理
 * **/

public final class ThreadException implements Thread.UncaughtExceptionHandler{

	private static final RaptorLogger LOGGER = new RaptorLogger(ThreadException.class);

	public ThreadException() {
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("业务线程执行异常,threadName: " + t.getName() + ", message: " + StringUtil.getErrorText(e));
	}

}
