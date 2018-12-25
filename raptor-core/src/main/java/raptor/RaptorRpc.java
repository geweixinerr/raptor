package raptor;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.PushMessageCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.exception.RpcException;
import raptor.util.StringUtil;

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
	public void sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, Integer timeOut,
			T... body) {
		ObjectPool<RpcPushDefine> pool = RpcClient.getRpcPoolMapping().get(serverName);
		if (pool == null) {
			LOGGER.error("RPC服务器映射不存在,请检查配置. serverName : " + serverName);
			throw new RpcException("RPC服务器映射不存在,请检查配置. serverName : " + serverName);
		}

		RpcPushDefine rpc = null;
		try {
			while (true) {
				rpc = pool.borrowObject();
				if (rpc.isWritable()) {
					break;
				} else {
					//入池等待数据可推送.
					pool.returnObject(rpc); 
				}
			}
		} catch (Exception e) {
			String message = StringUtil.getErrorText(e);
			LOGGER.error("RPC 连接池获取对象失败,message: " + message);
			throw new RpcException("RPC 连接池获取对象失败,message: " + message);
		}
		
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

		// 发送消息(异步发送)
		rpc.pushMessage(requestBody, new PushMessageCallBack(rpc) {
			@Override
			public void invoke() {
				//tcp连接入池,释放当前tcp连接占用.
				try {
					pool.returnObject(this.getRpcObject());
				} catch (Exception e) {
					//资源回收异常,默认不处理.
					LOGGER.error("资源池回收异常,requestBody: " + requestBody + ", message : " + StringUtil.getErrorText(e));
				}
			}
		}); 
		
		RpcClientTaskPool.pushTask(requestBody); // 入客户端队列,定时扫描.
		
		/**
		 * 客户端异步请求达到Netty Buffer高水平线,阻流.
		 * **/
		/*
		RpcResponseBody responseBody = new RpcResponseBody();
		responseBody.setSuccess(false);
		responseBody.setMessageId(requestBody.getMessageId());
		responseBody.setMessage("RPC 服务调用失败,message:[Netty Buffer高水平线,阻流]");
		responseBody.setRpcCode(RpcResult.FLOWER_CONTROL);
		call.invoke(responseBody); //直接回调输出结果.
		*/
	}

	// 重载异步方法
	@SuppressWarnings("unchecked")
	public void sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, T... body) {
		if (StringUtils.isBlank(serverName) || StringUtils.isBlank(rpcMethodName) || call == null) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		
	    sendAsyncMessage(serverName, rpcMethodName, call, TIME_OUT, body);
	}
	
}
