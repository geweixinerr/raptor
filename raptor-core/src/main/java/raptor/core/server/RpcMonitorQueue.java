package raptor.core.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * RPC Server端监视器队列,服务器主推/分发消息至客户端. 如主动判断心跳等
 * 
 * @author gewx
 * **/

public final class RpcMonitorQueue {

	public static final Map<String,Channel> SERVER_QUEUE =  new ConcurrentHashMap<String, Channel>(1024);  
	
	private RpcMonitorQueue() {

	}

}
