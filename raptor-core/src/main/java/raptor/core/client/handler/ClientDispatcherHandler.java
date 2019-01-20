package raptor.core.client.handler;

import java.net.InetSocketAddress;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.pool2.ObjectPool;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.util.StringUtil;

/**
 * @author gewx 客户端RPC入站请求处理器
 **/
@ThreadSafe
@Sharable
public final class ClientDispatcherHandler extends SimpleChannelInboundHandler<RpcResponseBody> implements RpcPushDefine {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcherHandler.class);
	
	private final ObjectPool<RpcPushDefine> pool;
	
	private ChannelHandlerContext ctx;
	
	private final String tcpId; 
	
	private final String serverNode;
	
	public ClientDispatcherHandler(String tcpId, String serverNode) {
		this.tcpId = tcpId;
		this.serverNode = serverNode;
		this.pool = RpcClient.getRpcPoolMapping().get(serverNode);
	}

	@Override
	public String getTcpId() {
		return this.tcpId;
	}

	@Override
	public ObjectPool<RpcPushDefine> getRpcPoolObject() {
		return this.pool;
	}

	@Override
	public void pushMessage(RpcRequestBody requestBody) {
		RpcPushDefine rpcObject = this;
		ChannelFuture future = ctx.writeAndFlush(requestBody);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					try {
						pool.returnObject(rpcObject);
					} catch (Exception e) {
						String message = StringUtil.getErrorText(e);
						LOGGER.error("资源释放异常,tcpId: "+rpcObject.getTcpId()+ ", serverNode: " + serverNode +", message: " + message);
						throw new RpcException("资源释放异常,tcpId: "+rpcObject.getTcpId()+ ", serverNode: " + serverNode +", message: " + message);
					}
 				} else {
					String message = StringUtil.getErrorText(future.cause());
					LOGGER.error("RPC请求推送异常,tcpId: "+rpcObject.getTcpId()+ ", serverNode: " + serverNode +", message: " + message);
 					pool.invalidateObject(rpcObject);
				}
			}
		});
	}
	
	@Override
	public void close() {
		RpcPushDefine rpc = this;
		ChannelFuture future = ctx.close();
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					LOGGER.info("tcp连接关闭成功,tcp_id: " + rpc.getTcpId());
				} else {
					LOGGER.error("tcp连接关闭失败,message: " + StringUtil.getErrorText(future.cause()));
				}
			}
		});
	}

	@Override
	public boolean isWritable() {
		Channel channel = ctx.channel();
		boolean isWritable = channel.isWritable();
		boolean isActive = channel.isActive();
		
		if (isWritable && isActive) {
			return true;
		} else {
		    return false;
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponseBody responseBody) throws Exception {
		if (responseBody.getRpcMethod().equals(HEARTBEAT_METHOD)) {
			LOGGER.warn("[重要!!!]tcp 心跳包收到响应,tcp_id: " + this.getTcpId());
			return;
		}
		LOGGER.info("客户端收到响应: " + responseBody);
		responseBody.setResponseTime(new DateTime());	
		RpcClientTaskPool.addTask(responseBody); 
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC IO异常,tcpId: "+this.getTcpId()+ ", serverNode: " + this.serverNode + ", message: " + message);
		pool.invalidateObject(this);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		Channel channel = ctx.channel();
		InetSocketAddress local = (InetSocketAddress) channel.localAddress();
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		
		LOGGER.warn("[重要!!!]心跳检测...,tcp_id: " + this.getTcpId() + ", 客户端: " + local.getAddress() + ":" + local.getPort()
				+", 服务器: " + remote.getAddress() + ":" + remote.getPort() + ", serverNode: " + this.serverNode 
				+ ", active: " + this.pool.getNumActive() +", Idle: " + this.pool.getNumIdle());
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(new UUID().toString());
		requestBody.setRpcMethod(HEARTBEAT_METHOD);
		ctx.writeAndFlush(requestBody);
	}
}
