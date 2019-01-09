package raptor;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.exception.RpcException;
import raptor.util.StringUtil;

/**
 * @author gewx Raptor RPC消息的包装发送.
 **/
public final class RaptorRpc<T extends Serializable> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RaptorRpc.class);
	
	/**
	 * 业务超时设置,默认5秒
	 **/
	private static final Integer TIME_OUT = 5;
	
	/**
	 * 延迟时间,单位:毫秒
	 * **/
	private static final Integer DELAY_TIME = 1;
	
	/**
	 * @author gewx 异步发送消息
	 * @param serverNode
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, call 回调对象,
	 *            timeOut 业务超时设置[单位/秒(默认5秒)],body 消息主体内容
	 * @throws RpcException RPC调用失败
	 * @return void
	 **/
	@SuppressWarnings({ "unchecked"})
	public void sendAsyncMessage(String serverNode, String rpcMethodName, AbstractCallBack call, Integer timeOut,
			T... body) throws RpcException {
		if (StringUtils.isBlank(serverNode) || StringUtils.isBlank(rpcMethodName) || call == null || timeOut == null) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		
		ObjectPool<RpcPushDefine> pool = RpcClient.getRpcPoolMapping().get(serverNode);

		if (pool == null) {
			LOGGER.error("RPC服务器映射不存在,请检查配置. serverNode : " + serverNode);
			throw new RpcException("RPC服务器映射不存在,请检查配置. serverNode : " + serverNode);
		}
		
		RpcPushDefine rpc = null;
		try {
			while (true) {
				rpc = pool.borrowObject();
				if (rpc.isWritable()) {
					break;
				}
			}
			
		} catch (Exception e) {
			String message = StringUtil.getErrorText(e);
			LOGGER.error("RPC 连接池获取对象失败,message: " + message);
			throw new RpcException("RPC 连接池获取对象失败,message: " + message);
		}
		
		String uuid = new UUID().toString();
		DateTime reqDate = new DateTime();
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setRequestTime(reqDate);
		requestBody.setTimeOut(reqDate.plusSeconds(timeOut));
		requestBody.setDelayTime(System.currentTimeMillis() + DELAY_TIME); //delay
		requestBody.setCall(call);

		RpcClientTaskPool.pushTask(requestBody); 
		rpc.pushMessage(requestBody);
		
	}

	@SuppressWarnings("unchecked")
	public void sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, T... body) throws RpcException {
		if (StringUtils.isBlank(serverName) || StringUtils.isBlank(rpcMethodName) || call == null) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		
	    sendAsyncMessage(serverName, rpcMethodName, call, TIME_OUT, body);
	}
	
}
