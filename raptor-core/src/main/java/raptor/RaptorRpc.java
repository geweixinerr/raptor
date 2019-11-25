package raptor;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.joda.time.DateTime;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.RpcResult;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.log.ThreadContext;
import raptor.util.StringUtil;

/**
 * @author gewx Raptor RPC消息的包装发送.
 **/
public final class RaptorRpc<T extends Serializable> {
	
	/**
	 * 业务超时设置,默认5秒
	 **/
	private static final Integer TIME_OUT = 5;
	
	
	/**
	 * @author gewx 同步发送消息
	 * @param serverNode
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, call 回调对象,
	 *            timeOut 业务超时设置[单位/秒(默认5秒)],body 消息主体内容
	 * @throws RpcException RPC调用失败
	 * @return void
	 **/
	@SuppressWarnings({ "unchecked"})
	public RpcResponseBody sendSyncMessage(String serverNode, String rpcMethodName, Integer timeOut, T... body) throws RpcException {
		if (StringUtils.isBlank(serverNode) || StringUtils.isBlank(rpcMethodName) || timeOut == null) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		
		ObjectPool<RpcPushDefine> pool = RpcClient.getRpcPoolMapping().get(serverNode);

		if (pool == null) {
			throw new RpcException("RPC服务器映射不存在,请检查配置! serverNode : " + serverNode, RpcResult.ERROR);
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
			if (e instanceof RpcException) {
				RpcException rpcEx = (RpcException) e;
				throw rpcEx;
			} else {
				String message = StringUtil.getErrorText(e);
				throw new RpcException("RPC 连接池获取对象失败,serverNode : " + serverNode + ", message: " + message, RpcResult.ERROR);
			}
		}
		
		BlockingQueue<RpcResponseBody> blockQueue = new LinkedBlockingQueue<RpcResponseBody>(1);
		String uuid = new UUID().toString();
		DateTime reqDate = new DateTime();
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setTraceId(ThreadContext.TRACEID.get());
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setRequestTime(reqDate);
		requestBody.setTimeOut(reqDate.plusSeconds(timeOut));
		requestBody.setCall(new AbstractCallBack() {
			@Override
			public void invoke(RpcResponseBody resp) {
				blockQueue.offer(resp);
			}
		});
		requestBody.setSync(true);
		RpcClientTaskPool.pushTask(requestBody); 
		rpc.pushMessage(requestBody);
		
		try {
			RpcResponseBody result = blockQueue.poll(timeOut, TimeUnit.SECONDS);
			// timeOut
			if (result == null) { 
				Integer rpcTime = StringUtil.timeDiffForMilliSecond(requestBody.getRequestTime(),new DateTime());
				// remove
				RpcClientTaskPool.listMapPool().remove(uuid); 
				result = new RpcResponseBody();
				result.setRpcCode(RpcResult.TIME_OUT);
				result.setMessage("服务调用超时.");
				result.setMessageId(uuid);
				result.setRpcTime(rpcTime);
			}
			return result;
		} catch (InterruptedException e) {
			String message = StringUtil.getErrorText(e);
			throw new RpcException("RPC调用异常,serverNode : " + serverNode + ", message: " + message, RpcResult.ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	public RpcResponseBody sendSyncMessage(String serverName, String rpcMethodName, T... body) throws RpcException {
		if (StringUtils.isBlank(serverName) || StringUtils.isBlank(rpcMethodName)) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		
		return sendSyncMessage(serverName, rpcMethodName, TIME_OUT, body);
	}
	
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
			throw new RpcException("RPC服务器映射不存在,请检查配置! serverNode : " + serverNode, RpcResult.ERROR);
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
			if (e instanceof RpcException) {
				RpcException rpcEx = (RpcException) e;
				throw rpcEx;
			} else {
				String message = StringUtil.getErrorText(e);
				throw new RpcException("RPC 连接池获取对象失败,serverNode : " + serverNode + ", message: " + message, RpcResult.ERROR);
			}
		}
		
		String uuid = new UUID().toString();
		DateTime reqDate = new DateTime();
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(uuid);
		requestBody.setTraceId(ThreadContext.TRACEID.get());
		requestBody.setBody(body);
		requestBody.setRpcMethod(rpcMethodName);
		requestBody.setRequestTime(reqDate);
		requestBody.setTimeOut(reqDate.plusSeconds(timeOut));
		requestBody.setCall(call);
		requestBody.setSync(false);

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
