package raptor;

import java.io.Serializable;

import raptor.core.AbstractCallBack;

/**
 * @author gewx Raptor消息发送入口类,消息的包装发送.
 **/
public final class RaptorRpc<T extends Serializable> {

	/**
	 * 业务超时设置,默认5秒
	 **/
	private static final Integer TIME_OUT = 5;

	/**
	 * @author gewx 同步发送消息
	 * @param serverName
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, object 消息主体内容, timeOut
	 *            业务超时设置,单位/秒(默认5秒)
	 * @return void
	 **/
	public void sendSyncMessage(String serverName, String rpcMethodName, T object, Integer timeOut) {

	}

	// 重载同步方法
	public void sendSyncMessage(String serverName, String rpcMethodName, T object, AbstractCallBack call) {
		sendSyncMessage(serverName, rpcMethodName, object, TIME_OUT);
	}

	/**
	 * @author gewx 异步发送消息
	 * @param serverName
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, object 消息主体内容, call 回调对象,
	 *            timeOut 业务超时设置,单位/秒(默认5秒)
	 * 
	 * @return void
	 **/
	public void sendAsyncMessage(String serverName, String rpcMethodName, T object, AbstractCallBack call,
			Integer timeOut) {

	}

	// 重载异步方法
	public void sendAsyncMessage(String serverName, String rpcMethodName, T object, AbstractCallBack call) {
		sendAsyncMessage(serverName, rpcMethodName, object, call, TIME_OUT);
	}

}
