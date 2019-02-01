package raptor.core.client.handler;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

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
import raptor.core.RpcResult;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.util.StringUtil;

/**
 * @author gewx 客户端RPC入站请求处理器
 **/
@ThreadSafe
@Sharable
public final class ClientDispatcherHandler extends SimpleChannelInboundHandler<RpcResponseBody> implements RpcPushDefine {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcherHandler.class);
	
	private static final Integer DEFAULT_HEARTBEAT_COUNT = 5;
	
	private final AtomicInteger heartbeatCount = new AtomicInteger();
	
	private final ObjectPool<RpcPushDefine> pool;
	
	private final String tcpId; 
	
	private final String serverNode;
	
	private ChannelHandlerContext ctx;
	
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
		RpcPushDefine rpc = this;
		ChannelFuture future = ctx.writeAndFlush(requestBody);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					try {
						pool.returnObject(rpc);
					} catch (Exception e) {
						String message = StringUtil.getErrorText(e);
						LOGGER.error("资源释放异常,tcpId: " + rpc.getTcpId() + ", serverNode: " + serverNode + ", message: " + message);
						pool.invalidateObject(rpc);
					}
 				} else {
 					outboundException(requestBody.getMessageId(), "Rpc出站Fail.", RpcResult.FAIL_NETWORK_TRANSPORT);
					String message = StringUtil.getErrorText(future.cause()); 
					LOGGER.warn("RPC客户端数据出站FAIL: " + requestBody + ", tcpId: " + rpc.getTcpId() + ", serverNode: " + serverNode + ", message: " + message);	
 					pool.invalidateObject(rpc);
				}
			}
		});
	}
	
	@Override
	public void close() {
		ctx.close();
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
	public void returnClean() {
		heartbeatCount.set(0);
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponseBody responseBody) throws Exception {
		if (responseBody.getRpcMethod().equals(HEARTBEAT_METHOD)) {
			heartbeatCount.set(0);
			LOGGER.warn("[重要!!!]tcp 心跳包收到响应,tcpId: " + getTcpId() + ", serverNode: " + serverNode);
			return;
		}

		responseBody.setResponseTime(new DateTime());	
		RpcClientTaskPool.addTask(responseBody); 
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC IO异常,tcpId: "+ getTcpId() + ", serverNode: " + serverNode + ", message: " + message);
		pool.invalidateObject(this);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		heartbeatCount.incrementAndGet();
		
		Channel channel = ctx.channel();
		InetSocketAddress local = (InetSocketAddress) channel.localAddress();
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		
		if (heartbeatCount.intValue() >= DEFAULT_HEARTBEAT_COUNT) {
			LOGGER.warn("[重要!!!]心跳检测无响应, tcpId: " + getTcpId() + ", 客户端: " + local.getAddress() + ":" + local.getPort()
			+ ", 服务器: " + remote.getAddress() + ":" + remote.getPort() + ", serverNode: " + serverNode 
			+ ", active: " + pool.getNumActive() +", Idle: " + pool.getNumIdle());
			pool.invalidateObject(this);
			return;
		}
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(new UUID().toString());
		requestBody.setRpcMethod(HEARTBEAT_METHOD);
		requestBody.setBody(new String[] {getTcpId()});
		ChannelFuture future = ctx.writeAndFlush(requestBody);
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				StringBuilder state = new StringBuilder();
				if (future.isSuccess()) {
					state.append("SUCCESS");
				} else {
					state.append("FAIL");
				}
				
				LOGGER.warn("[重要!!!]心跳检测,发送" + state.toString() + ", tcpId: " + getTcpId() + ", 客户端: " + local.getAddress() + ":" + local.getPort()
				+ ", 服务器: " + remote.getAddress() + ":" + remote.getPort() + ", serverNode: " + serverNode 
				+ ", active: " + pool.getNumActive() +", Idle: " + pool.getNumIdle());
			}
		});
		
	}
	
	private static void outboundException(String messageId, String message, RpcResult rpcCode) {
		RpcResponseBody responseBody = new RpcResponseBody();
		responseBody.setRpcCode(rpcCode);
		responseBody.setMessage(message);
		responseBody.setMessageId(messageId);
		responseBody.setResponseTime(new DateTime());	
		RpcClientTaskPool.addTask(responseBody); 
	}
	
}
