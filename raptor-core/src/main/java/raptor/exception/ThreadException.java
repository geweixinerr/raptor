package raptor.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raptor.util.StringUtil;


/**
 * @author gewx 线程异常处理
 * **/

public final class ThreadException implements Thread.UncaughtExceptionHandler{

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadException.class);

	public ThreadException() {
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("业务线程执行异常,threadName: " + t.getName() +", message: " + StringUtil.getErrorText(e));
	}

}
