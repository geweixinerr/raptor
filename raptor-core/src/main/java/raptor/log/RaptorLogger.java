package raptor.log;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.eaio.uuid.UUID;

/**
 * @author gewx Raptor PRC 日志框架
 **/
public final class RaptorLogger {
	
	public static final TransmittableThreadLocal<String> THREAD_ID = new TransmittableThreadLocal<String>() {
		@Override
		protected String initialValue() {
			String invokeId = new UUID().toString();
			return invokeId;
		}
	};

	enum LOGGER {
		enter, exit, info, warn, error
	}

	private static final String MDC_STATE = "state";
	
	private static final String MDC_INVOKE = "invokeId";

	private final Logger logger;

	@SuppressWarnings("rawtypes")
	public RaptorLogger(Class c) {
		this.logger = LoggerFactory.getLogger(c);
	}

	public void enter(String methodName, String msg, boolean repeatThreadId) {		
		if (!repeatThreadId) {
			THREAD_ID.set(new UUID().toString());
		}
		
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName) + " | " + LOGGER.enter);
		MDC.put(MDC_INVOKE, THREAD_ID.get());
		logger.info(msg);
		MDC.clear();
	}
	
	public void enter(String methodName, String msg) {		
		enter(methodName, msg, true);
	}

	public void enter(String msg, boolean repeatThreadId) {
		enter(null, msg, repeatThreadId);
	}
	
	public void enter(String msg) {
		enter(null, msg);
	}

	public void exit(String methodName, String msg) {
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName) + " | " + LOGGER.exit);
		MDC.put(MDC_INVOKE, THREAD_ID.get());
		logger.info(msg);
		MDC.clear();
	}

	public void exit(String msg) {
		exit(null, msg);
	}

	public void info(String methodName, String msg, boolean repeatThreadId) {
		if (!repeatThreadId) {
			THREAD_ID.set(new UUID().toString());
		}
		
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName));
		MDC.put(MDC_INVOKE, THREAD_ID.get());
		logger.info(msg);		
		MDC.clear();
	}
	
	public void info(String methodName, String msg) {
		info(methodName, msg, true);
	}

	public void info(String msg, boolean repeatThreadId) {
		info(null, msg, repeatThreadId);
	}
	
	public void info(String msg) {
		info(null, msg);
	}

	public void warn(String methodName, String msg) {
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName));
		MDC.put(MDC_INVOKE, THREAD_ID.get());
		logger.warn(msg);
		MDC.clear();
	}

	public void warn(String msg) {
		warn(null, msg);
	}

	public void error(String methodName, String msg) {
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName));
		MDC.put(MDC_INVOKE, THREAD_ID.get());
		logger.error(msg);
		MDC.clear();
	}

	public void error(String msg) {
		error(null, msg);
	}
	
}
