package raptor.log;

import com.alibaba.ttl.TransmittableThreadLocal;

public final class ThreadContext {
	
	public static final TransmittableThreadLocal<String> TRACEID = new TransmittableThreadLocal<String>();
	
}
