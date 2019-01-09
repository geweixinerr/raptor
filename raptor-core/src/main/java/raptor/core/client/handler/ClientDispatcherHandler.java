package raptor.core.client.handler;

import java.net.InetSocketAddress;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.pool2.ObjectPool;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

	/**
	 * 时间格式化
	 * **/
    private  static final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");
	
	/**
	 * 线程安全的ChannelHandlerContext实例对象.
	 * **/
	private ChannelHandlerContext ctx;
	
	/**
	 * tcpId,唯一标识单条tcp连接
	 * **/
	private final String tcpId; 
	
	/**
	 * 当前tcp连接隶属的tcp pool池
	 * **/
	private final ObjectPool<RpcPushDefine> pool;
	
	/**
	 * 速率
	 * **/
	private final Integer speedNum;
	
	/**
	 * tcp连接入池时间 [仅推荐测试使用]
	 * **/
	private final DateTime into_pool_time;
	
	/**
	 * 速率控制对象计数
	 * **/
	private final AtomicInteger speedObject = new AtomicInteger();
	
	/**
	 * 延迟发包Queue
	 * **/
	private final DelayQueue<RpcRequestBody> queue = new DelayQueue<RpcRequestBody>();
	
	/**
	 * tcp包是否延迟发送中标记.
	 * **/
	private boolean isPush = false;
	
	/**
	 * tcp 连接状态
	 * **/
	private boolean state = true;
	
	public ClientDispatcherHandler(String tcpId, String serverNode, Integer speedNum) {
		this.tcpId = tcpId;
		this.into_pool_time = new DateTime();
		this.speedNum = speedNum;
		this.pool = RpcClient.getRpcPoolMapping().get(serverNode);
	}

	@Override
	public String getTcpId() {
		return this.tcpId;
	}

	@Override
	public DateTime getTcpIntoPoolTime() {
		return this.into_pool_time;
	}

	@Override
	public ObjectPool<RpcPushDefine> getRpcPoolObject(){
		return this.pool;
	}
	
	@Override
	public void setState(boolean bool) {
		this.state = bool;
	}

	@Override
	public boolean getState() {
		return this.state;
	}

	/**
	 * @author gewx RPC实际调用--->信息推送.
	 **/
	@Override
	public void pushMessage(RpcRequestBody requestBody) {
		try {
			queue.add(requestBody);			
			loopPushMessage();
		} finally {
			speedObject.incrementAndGet();
			if (speedObject.intValue() < speedNum) {
				try {
					pool.returnObject(this);
				} catch (Exception e) {
					LOGGER.error("资源释放异常,tcpId: "+this.getTcpId()+",message: " + StringUtil.getErrorText(e));
					throw new RpcException("资源释放异常,tcpId: "+this.getTcpId()+",message: " + StringUtil.getErrorText(e));
				}
			}
		}
	}
	
	/**
	 * @author gewx 关闭闲时的tcp连接.
	 * **/
	@Override
	public void close() {
		RpcPushDefine rpc = this;
		ChannelFuture future = ctx.close();
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					LOGGER.info("tcp连接关闭成功,tcp_id: " + rpc.getTcpId());
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("tcp_id: " + rpc.getTcpId() + ", tcp连接服务周期账单,明细[入池时间: " + rpc.getTcpIntoPoolTime().toString(dateTimeFormat) 
								+ ", 出池时间: " + new DateTime().toString(dateTimeFormat) + "]");
					}
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
		
		responseBody.setResponseTime(new DateTime());	
		RpcClientTaskPool.addTask(responseBody); 
		int thisSpeed = speedObject.intValue();
		speedObject.decrementAndGet();
		if (thisSpeed == speedNum) {
			synchronized (this) {
				if (!this.getState()) {
					try {
						pool.returnObject(this);
					} catch (Exception e) {
						LOGGER.error("资源释放异常,tcpId: "+this.getTcpId()+",message: " + StringUtil.getErrorText(e));
						throw new RpcException("资源释放异常,tcpId: "+this.getTcpId()+",message: " + StringUtil.getErrorText(e));
					}
				}
			}	
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC客户端异常,message: " + message);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		Channel channel = ctx.channel();
		InetSocketAddress local = (InetSocketAddress) channel.localAddress();
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		
		LOGGER.warn("[重要!!!]心跳检测...,tcp_id: " + this.getTcpId() + ", 客户端: " + local.getAddress() + ":" + local.getPort()
				+", 服务器: " + remote.getAddress() + ":" + remote.getPort() 
				+", active: " + this.pool.getNumActive() +", Idle: " + this.pool.getNumIdle());
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(new UUID().toString());
		requestBody.setRpcMethod(HEARTBEAT_METHOD);
		ctx.writeAndFlush(requestBody);
	}

	/**
	 * 循环延迟推送,减少包的传输速率.
	 * **/
	private void loopPushMessage() {
		if (!this.isPush) {
			this.isPush = true;
			while (queue.size() > 0) {
				RpcRequestBody requestBody = queue.poll();
				if (requestBody != null) {
					ctx.writeAndFlush(requestBody);
				}
			}
			this.isPush = false;  
		}
	}
}
