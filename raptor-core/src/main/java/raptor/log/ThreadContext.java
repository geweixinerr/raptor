package raptor.log;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.eaio.uuid.UUID;

public final class ThreadContext {
	
	public static final TransmittableThreadLocal<String> TRACEID = new TransmittableThreadLocal<String>() {
		@Override
		protected String initialValue() {
			String invokeId = new UUID().toString();
			return invokeId;
		}
	};
	
}
