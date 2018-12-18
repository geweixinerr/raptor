package raptor.test;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;

/**
 * @author gewx Raptor消息发送入口类,消息的包装发送.
 **/
public final class TestRaptorRpc<T extends Serializable> {

	/**
	 * 业务超时设置,默认5秒
	 **/
	private static final Integer TIME_OUT = 5;

	/**
	 * @author gewx 测试专供
	 **/
	public static RpcPushDefine rpcClient = null;

	/**
	 * @author gewx 异步发送消息
	 * @param serverName
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, body 消息主体内容, call
	 *            回调对象, timeOut 业务超时设置,单位/秒(默认5秒)
	 * 
	 * @return void
	 **/
	@SuppressWarnings("unchecked")
	public void sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, Integer timeOut,
			T... body) {
		
        DateTime reqDate = new DateTime(); //请求时间
		
		String uuid = new UUID().toString();
		// 组装请求参数
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setRequestTime(reqDate);
		requestBody.setTimeOut(reqDate.plusSeconds(timeOut));
		requestBody.setCall(call);

		rpcClient.pushMessage(requestBody); // 发送消息(异步发送)
		RpcClientTaskPool.pushTask(requestBody); // 入客户端队列.
	}

	// 重载异步方法
	@SuppressWarnings("unchecked")
	public void sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, T... body) {
		sendAsyncMessage(serverName, rpcMethodName, call, TIME_OUT, body);
	}

}
