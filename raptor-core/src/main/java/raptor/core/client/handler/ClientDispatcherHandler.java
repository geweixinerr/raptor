package raptor.core.client.handler;

import javax.annotation.concurrent.ThreadSafe;

import org.joda.time.DateTime;
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
import raptor.exception.RpcException;
import raptor.util.StringUtil;

/**
 * @author gewx 客户端入站处理器
 **/
@ThreadSafe
@Sharable
public class ClientDispatcherHandler extends SimpleChannelInboundHandler<RpcResponseBody> implements RpcPushDefine {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcherHandler.class);

	private ChannelHandlerContext ctx;
	
	private final String tcp_id; //tcp连接唯一标识
	
	public ClientDispatcherHandler(String tcp_id) {
		this.tcp_id = tcp_id;
	}

	@Override
	public String getTcpId() {
		return this.tcp_id;
	}

	/**
	 * @author gewx 消息推送
	 **/
	@Override
	public void pushMessage(RpcRequestBody requestBody, PushMessageCallBack call) {
		ChannelFuture future = ctx.writeAndFlush(requestBody);
		try {
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {

				}
			});
		} finally {
			call.invoke(); //回调,tcp连接资源入池.
		}
	}
	
	
	@Override
	public void close() {
		ChannelFuture future = ctx.close();
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					LOGGER.info("服务关闭成功.");
				} else {
					LOGGER.error("服务关闭失败,message: " + StringUtil.getErrorText(future.cause()));
				}
			}
		});
	}

	@Override
	public boolean isWritable() {
		Channel channel = ctx.channel();
		boolean isWritable = channel.isWritable();
		boolean isOpen = channel.isOpen();
		boolean isActive = channel.isActive();
		
		if (isWritable && isOpen && isActive) {
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
		msg.setResponseTime(new DateTime());		
		RpcClientTaskPool.addTask(msg); // 入池处理.
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC客户端异常,message: " + message);
	}

}
