package raptor.log;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 日志线程上下文类
 * 
 * @author gewx
 **/

public final class ThreadContext {
	
	public static final TransmittableThreadLocal<String> TRACEID = new TransmittableThreadLocal<String>();
	
}
