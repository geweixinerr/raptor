package raptor;

import java.io.Serializable;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClientRegistry;
import raptor.core.client.RpcClientRegistry.rpcEnum;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;

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
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, body 消息主体内容, timeOut
	 *            业务超时设置,单位/秒(默认5秒)
	 * 
	 * @return void
	 **/
	public void sendSyncMessage(String serverName, String rpcMethodName, T[] body, Integer timeOut) {
		RpcPushDefine rpcClient = RpcClientRegistry.INSTANCE.getRpcClient(rpcEnum.rpcPushDefine);

		String uuid = new UUID().toString();
		// 组装请求参数
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setTimeOut(timeOut);

		rpcClient.pushMessage(requestBody); // 发送消息
	}

	// 重载同步方法
	public void sendSyncMessage(String serverName, String rpcMethodName, T[] body) {
		sendSyncMessage(serverName, rpcMethodName, body, TIME_OUT);
	}

	/**
	 * @author gewx 异步发送消息
	 * @param serverName
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, body 消息主体内容, call 回调对象,
	 *            timeOut 业务超时设置,单位/秒(默认5秒)
	 * 
	 * @return void
	 **/
	public void sendAsyncMessage(String serverName, String rpcMethodName, T[] body, AbstractCallBack call,
			Integer timeOut) {
		RpcPushDefine rpcClient = RpcClientRegistry.INSTANCE.getRpcClient(rpcEnum.rpcPushDefine);

		String uuid = new UUID().toString();
		// 组装请求参数
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setTimeOut(timeOut);

		rpcClient.pushMessage(requestBody); // 发送消息(异步发送)
		RpcClientTaskPool.pushTask(requestBody); //入队列.
	}

	// 重载异步方法
	public void sendAsyncMessage(String serverName, String rpcMethodName, T[] body, AbstractCallBack call) {
		sendAsyncMessage(serverName, rpcMethodName, body, call, TIME_OUT);
	}

}
