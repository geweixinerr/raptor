package raptor;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import raptor.core.AbstractCallBack;
import raptor.core.PushMessageCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.TcpPreventCongestion;
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

	private static final TcpPreventCongestion TCP_PREVENT_CONGESTION = TcpPreventCongestion.INSTANCE;
	
	/**
	 * 业务超时设置,默认5秒
	 **/
	private static final Integer TIME_OUT = 5;
	
	/**
	 * @author gewx 异步发送消息
	 * @param serverName
	 *            服务名(配置在客户端配置当中), rpcMethodName 调用服务方法名, call 回调对象,
	 *            timeOut 业务超时设置[单位/秒(默认5秒)],body 消息主体内容
	 * @throws RpcException RPC调用失败
	 * @return void
	 **/
	@SuppressWarnings({ "unchecked"})
	public void sendAsyncMessage(String serverName, String rpcMethodName, AbstractCallBack call, Integer timeOut,
			T... body) {
		if (StringUtils.isBlank(serverName) || StringUtils.isBlank(rpcMethodName) || call == null || timeOut == null) {
			throw new IllegalArgumentException("缺失服务请求参数,serverName/rpcMethodName/call isNotEmpty!");
		}
		
		ObjectPool<RpcPushDefine> pool = RpcClient.getRpcPoolMapping().get(serverName);
		if (pool == null) {
			LOGGER.error("RPC服务器映射不存在,请检查配置. serverName : " + serverName);
			throw new RpcException("RPC服务器映射不存在,请检查配置. serverName : " + serverName);
		}
        		
		Queue<RpcPushDefine> cacheRpcQueue = new ArrayDeque<RpcPushDefine>(32); //缓存暂时无法使用的池内对象.
		RpcPushDefine rpc = null;
		try {
			while (true) {
				rpc = pool.borrowObject();
				if (rpc.isWritable()) {
					break;
				} else {
					/**
					 * 入池等待数据可推送.按照FIFO规则入池,这里存在一个问题,简单描述如下：
					 * 这里入池后直至出池这段时间数据,假如rpc对象push不及时,则会再度入池,依次循环. 存在较大时间的循环获取池内对象的可能性
					 * pool.returnObject(rpc); //不推荐直接入池 
					 * 建议采用如下的编码方式,强制创建新tcp连接发送数据.
					 * **/
					cacheRpcQueue.add(rpc); //推荐! 将无法使用的对象先入缓存器,强制tcp连接池创建更多的有效连接.
				}
			}
			
		} catch (Exception e) {
			String message = StringUtil.getErrorText(e);
			LOGGER.error("RPC 连接池获取对象失败,message: " + message);
			throw new RpcException("RPC 连接池获取对象失败,message: " + message);
		}
					
		TCP_PREVENT_CONGESTION.congestion(1); //防tcp拥塞

		//LOGGER.info("激活POOL Object数量: " + pool.getNumActive()+", TcpId: " + rpc.getTcpId());	
		
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
				try {
					pool.returnObject(this.getRpcObject()); //tcp连接入池,释放当前tcp连接占用.
					
					RpcPushDefine releaseObject = null;
					while ((releaseObject = cacheRpcQueue.poll()) != null) { //有效连接push成功,释放缓存区内连接。
						pool.returnObject(releaseObject);
					}
				} catch (Exception e) {
					//资源回收异常,默认不处理.
					LOGGER.error("资源池回收异常, message : " + StringUtil.getErrorText(e));
				}
			}
		}); 
		
		RpcClientTaskPool.pushTask(requestBody); // 入客户端队列,定时扫描.
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
