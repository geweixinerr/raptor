package raptor.log;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

/**
 * Raptor PRC 日志框架
 * 
 * @author gewx
 **/
public final class RaptorLogger {

	enum LOGGER {
		// 开始
		enter,

		// 退出
		exit,

		// 普通
		info,

		// 警告
		warn,

		// 异常
		error
	}

	private static final String MDC_STATE = "state";

	private static final String MDC_TRACEID = "traceId";

	private final Logger logger;

	@SuppressWarnings("rawtypes")
	public RaptorLogger(Class c) {
		this.logger = LoggerFactory.getLogger(c);
	}

	/**
	 * @author gewx 日志打印
	 * @param methodName 方法名, msg 打印消息body, isRepeatTraceId 是否采用新的traceId
	 *                   [默认true:不覆盖,false:覆盖] 备注:方法调用链最头部部分需要设置traceId,以用来追踪分布式日志栈.
	 *                   其它场景的日志打印无需关注这个入参,所谓调用链最头部,譬如:面向外部的Web服务的入口处,如servlet等
	 * @return void
	 **/
	public void enter(String methodName, String msg, boolean isRepeatTraceId) {
		if (!isRepeatTraceId) {
			ThreadContext.TRACEID.set(new UUID().toString());
		}

		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName) + " | " + LOGGER.enter);
		MDC.put(MDC_TRACEID, ThreadContext.TRACEID.get());
		logger.info(msg);
		MDC.clear();
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名, msg 打印消息body
	 * @return void
	 **/
	public void enter(String methodName, String msg) {
		enter(methodName, msg, true);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名, msg 打印消息body, isRepeatTraceId 是否采用新的traceId
	 * @return void
	 **/
	public void enter(String msg, boolean isRepeatTraceId) {
		enter(null, msg, isRepeatTraceId);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param msg 打印消息body
	 * @return void
	 **/
	public void enter(String msg) {
		enter(null, msg);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名 , msg 打印消息body
	 * @return void
	 **/
	public void exit(String methodName, String msg) {
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName) + " | " + LOGGER.exit);
		MDC.put(MDC_TRACEID, ThreadContext.TRACEID.get());
		logger.info(msg);
		MDC.clear();
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param msg 打印消息body
	 * @return void
	 **/
	public void exit(String msg) {
		exit(null, msg);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名, msg 打印消息body, isRepeatTraceId 是否采用新的traceId
	 * @return void
	 **/
	public void info(String methodName, String msg, boolean isRepeatTraceId) {
		if (!isRepeatTraceId) {
			ThreadContext.TRACEID.set(new UUID().toString());
		}

		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName));
		MDC.put(MDC_TRACEID, ThreadContext.TRACEID.get());
		logger.info(msg);
		MDC.clear();
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名, msg 打印消息body
	 * @return void
	 **/
	public void info(String methodName, String msg) {
		info(methodName, msg, true);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param msg 打印消息body, isRepeatTraceId 是否采用新的traceId
	 * @return void
	 **/
	public void info(String msg, boolean isRepeatTraceId) {
		info(null, msg, isRepeatTraceId);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param msg 打印消息body
	 * @return void
	 **/
	public void info(String msg) {
		info(null, msg);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名, msg 打印消息body
	 * @return void
	 **/
	public void warn(String methodName, String msg) {
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName));
		MDC.put(MDC_TRACEID, ThreadContext.TRACEID.get());
		logger.warn(msg);
		MDC.clear();
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param msg 打印消息body
	 * @return void
	 **/
	public void warn(String msg) {
		warn(null, msg);
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param methodName 方法名, msg 打印消息body
	 * @return void
	 **/
	public void error(String methodName, String msg) {
		MDC.put(MDC_STATE, StringUtils.trimToEmpty(methodName));
		MDC.put(MDC_TRACEID, ThreadContext.TRACEID.get());
		logger.error(msg);
		MDC.clear();
	}

	/**
	 * 日志打印
	 * 
	 * @author gewx
	 * @param msg 打印消息body
	 * @return void
	 **/
	public void error(String msg) {
		error(null, msg);
	}
}
