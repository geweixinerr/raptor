package raptor.core.client.handler;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

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
import raptor.core.PushMessageCallBack;
import raptor.core.RpcPushDefine;
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

	/**
	 * 时间格式化
	 * **/
    private  static final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");

	/**
	 * 线程安全的ChannelHandlerContext实例对象.
	 * **/
	private ChannelHandlerContext ctx;
	
	/**
	 * tcp隶属pool服务节点
	 * **/
	private final String serverNode;
	
	/**
	 * tcp_id,唯一标识单条tcp连接[tcp pool测试使用]
	 * **/
	private final String tcp_id; 
	
	/**
	 * tcp连接入池时间 [仅推荐测试使用]
	 * **/
	private final DateTime into_pool_time;
	
	/**
	 * tcp速率控制对象.
	 * **/
	private final AtomicInteger speedObject = new AtomicInteger(); 
	
	public ClientDispatcherHandler(String tcp_id, String serverNode) {
		this.tcp_id = tcp_id;
		this.serverNode = serverNode;
		this.into_pool_time = new DateTime();
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
	 * @author gewx RPC实际调用--->信息推送. 信息成功推送入Netty队列后,释放TCP连接入池.
	 * 后续未完事项:这里可以做tcp连接活动监控,譬如耗时总长,譬如单条tcp连接服务器保活等...
	 **/
	@Override
	public void pushMessage(RpcRequestBody requestBody, PushMessageCallBack call) {
		try {
			speedObject.incrementAndGet(); 
			ctx.writeAndFlush(requestBody);
		} finally {
			if (requestBody.getRpcMethod().equals(HEARTBEAT_METHOD)) { //心跳包的响应,无需释放tcp pool资源
				LOGGER.warn("[重要!!!]tcp 心跳包收到响应,tcp_id: " + this.getTcpId()); //打印即可.
			} else {
				call.invoke();	
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
					//客户端单方面强制断开连接
					ctx.disconnect();
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
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponseBody msg) throws Exception {
		speedObject.decrementAndGet();
		msg.setResponseTime(new DateTime());	
		RpcClientTaskPool.addTask(msg); // 入池处理.
	}

	/**
	 * 当一个Channel的可写的状态发生改变的时候执行，用户可以保证写的操作不要太快，这样可以防止OOM,写的太快容易发生OOM,
	 *  如果当发现Channel变得再次可写之后重新恢复写入的操作，Channel中的isWritable方法可以监控该channel的可写状态，
	 *  可写状态的阀门直接通过Channel.config().setWriterHighWaterMark()和Channel.config().setWriteLowWaterMark()配置
	 * **/
	/*
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		super.channelWritabilityChanged(ctx);
	}
	*/

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC客户端异常,message: " + message);
		//待定处理,响应客户端异常.
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		Channel channel = ctx.channel();
		InetSocketAddress local = (InetSocketAddress) channel.localAddress();
		InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
		
		LOGGER.warn("[重要!!!]心跳检测...,tcp_id: " + this.getTcpId() + ",客户端: " + local.getAddress() + ":" + local.getPort()
				+", 服务器: " + remote.getAddress() + ":" + remote.getPort());
		
		// 组装心跳检测包
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setMessageId(new UUID().toString());
		requestBody.setRpcMethod(HEARTBEAT_METHOD);
		requestBody.setRequestTime(new DateTime());
		
		this.pushMessage(requestBody, null); //发送心跳
	}

}
