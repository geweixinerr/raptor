package raptor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;

/**
 * @author gewx 心跳服务
 * **/

@RpcHandler
public final class Heartbeat {

	private static final Logger LOGGER = LoggerFactory.getLogger(Heartbeat.class);

	/**心跳**/
	@RpcMethod
	public void heartbeat(String tcpId) {
		LOGGER.warn("================心跳检测,tcp_id: " +tcpId+ "================");
	}
	
}
