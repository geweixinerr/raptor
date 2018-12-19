package raptor;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClientRegistry;
import raptor.core.client.RpcClientRegistry.rpcEnum;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;

/**
 * @author gewx Raptor消息发送入口类,消息的包装发送.
 **/
public final class RaptorRpc<T extends Serializable> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RaptorRpc.class);

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
	/*
	@SuppressWarnings("unchecked")
	public void sendSyncMessage(String serverName, String rpcMethodName, Integer timeOut, T... body) {
		RpcPushDefine rpcClient = RpcClientRegistry.INSTANCE.getRpcClient(rpcEnum.rpcPushDefine);

		//组装时间对象,并设置超时.
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, timeOut);
		
		String uuid = new UUID().toString();
		// 组装请求参数
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setTimeOut(cal.getTime());

		rpcClient.pushMessage(requestBody); // 发送消息
	}

	// 重载同步方法
	@SuppressWarnings("unchecked")
	public void sendSyncMessage(String serverName, String rpcMethodName, T... body) {
		sendSyncMessage(serverName, rpcMethodName, TIME_OUT, body);
	}
    */
	
	/**
	 * @author gewx 异步发送消息
	 * @param serverName
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, body 消息主体内容, call 回调对象,
	 *            timeOut 业务超时设置,单位/秒(默认5秒)
	 * 
	 * @return 服务请求受理结果, true : 受理成功, false: 受理失败,服务拒绝[超过raptor中间件发送的数据包上限,参考属性: ChannelOption.WRITE_BUFFER_WATER_MARK]
	 **/
	@SuppressWarnings("unchecked")
	public boolean sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, Integer timeOut,
			T... body) {
		RpcPushDefine rpcClient = RpcClientRegistry.INSTANCE.getRpcClient(rpcEnum.rpcPushDefine);
		
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

		boolean isMessageSend = rpcClient.pushMessage(requestBody); // 发送消息(异步发送)
		if (isMessageSend) {
			RpcClientTaskPool.pushTask(requestBody); // 入客户端队列.	
		} else {
			/**
			 * 客户端异步请求达到Netty Buffer高水平线,阻流.
			 * **/
			RpcResponseBody responseBody = new RpcResponseBody();
			responseBody.setSuccess(false);
			responseBody.setMessageId(requestBody.getMessageId());
			responseBody.setMessage("RPC 服务调用失败,message:[Netty Buffer高水平线,阻流]");
			call.invoke(responseBody); //直接回调输出结果.
		}
		
		return isMessageSend;
	}

	// 重载异步方法
	@SuppressWarnings("unchecked")
	public boolean sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, T... body) {
		if (StringUtils.isBlank(serverName) || StringUtils.isBlank(rpcMethodName) || call == null) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		return sendAsyncMessage(serverName, rpcMethodName, call, TIME_OUT, body);
	}

}
