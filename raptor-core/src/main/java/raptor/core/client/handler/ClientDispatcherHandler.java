package raptor.core.client.handler;

import javax.annotation.concurrent.ThreadSafe;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * tcp_id,唯一标识单条tcp连接[tcp pool测试使用]
	 * **/
	private final String tcp_id; 
	
	/**
	 * tcp连接入池时间 [仅推荐测试使用]
	 * **/
	private final DateTime into_pool_time;
	
	public ClientDispatcherHandler(String tcp_id) {
		this.tcp_id = tcp_id;
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
			ctx.writeAndFlush(requestBody);
		} finally {
			call.invoke();
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
		/**
		 * 客户端接收到服务器响应时间[仅推荐测试使用].
		 * **/
		if (LOGGER.isDebugEnabled()) {
			msg.setResponseTime(new DateTime());	
		}
		RpcClientTaskPool.addTask(msg); // 入池处理.
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC客户端异常,message: " + message);
		//待定处理,响应客户端异常.
	}

}
