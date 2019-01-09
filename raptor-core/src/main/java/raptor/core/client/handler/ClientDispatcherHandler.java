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
	 * tcp_id,唯一标识单条tcp连接[tcp pool测试使用]
	 * **/
	private final String tcp_id; 
	
	/**
	 * tcp连接入池时间 [仅推荐测试使用]
	 * **/
	private final DateTime into_pool_time;
	
	/**
	 * 当前tcp连接隶属的tcp pool池
	 * **/
	private final ObjectPool<RpcPushDefine> pool;
	
	/**
	 * 速率
	 * **/
	private final Integer speedNum;
	
	/**
	 * 速率控制对象计数
	 * **/
	private final AtomicInteger speedObject = new AtomicInteger();
	
	/**
	 * 速率释放对象计数
	 * **/
	private final AtomicInteger releaseObject = new AtomicInteger();
	
	/**
	 * 延迟执行tcp包发送
	 * **/
	private final DelayQueue<RpcRequestBody> queue = new DelayQueue<RpcRequestBody>();
	
	/**
	 * tcp包推送标记.
	 * **/
	private volatile boolean isPush = false;
	
	public ClientDispatcherHandler(String tcp_id, String serverNode, Integer speedNum) {
		this.tcp_id = tcp_id;
		this.into_pool_time = new DateTime();
		this.speedNum = speedNum;
		this.pool = RpcClient.getRpcPoolMapping().get(serverNode);
	}

	@Override
	public String getTcpId() {
		return this.tcp_id;
	}

	@Override
	public DateTime getTcpIntoPoolTime() {
		return this.into_pool_time;
	}

	/**
	 * @author gewx RPC实际调用--->信息推送.
	 **/
	@Override
	public void pushMessage(RpcRequestBody requestBody) {
		try {
			requestBody.setDelayTime(System.currentTimeMillis() + 1);
			queue.add(requestBody);			
			loopPush();
		} finally {
			if (requestBody.getRpcMethod().equals(HEARTBEAT_METHOD)) {
				return;
			}
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
		
		releaseObject.incrementAndGet();
		if (releaseObject.intValue() > (speedNum/2) && speedObject.intValue() == speedNum) {
			synchronized (this) {
				if (speedObject.intValue() == speedNum) {
					speedObject.set(speedObject.intValue() - releaseObject.intValue());
					releaseObject.set(0);
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
		
		LOGGER.warn("[重要!!!]心跳检测...,tcp_id: " + this.getTcpId() + ",客户端: " + local.getAddress() + ":" + local.getPort()
				+", 服务器: " + remote.getAddress() + ":" + remote.getPort());
		
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(new UUID().toString());
		requestBody.setRpcMethod(HEARTBEAT_METHOD);
		requestBody.setRequestTime(new DateTime());
		
		this.pushMessage(requestBody);
	}

	/**
	 * 循环延迟推送,减少包的传输速率.
	 * **/
	private void loopPush() {
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
