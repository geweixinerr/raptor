package raptor.core.client.handler;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.RpcPushDefine;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.util.StringUtil;

/**
 * @author gewx 客户端入站处理器
 **/
@ThreadSafe
@Sharable
public class ClientDispatcherHandler extends SimpleChannelInboundHandler<RpcResponseBody> implements RpcPushDefine {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcherHandler.class);

	private ChannelHandlerContext ctx;

	/**
	 * @author gewx 消息推送
	 **/
	@Override
	public boolean pushMessage(RpcRequestBody requestBody) {
		Channel channel = ctx.channel();
		if (channel.isWritable()) {
			ctx.writeAndFlush(requestBody); 
			return true;
		} else {
			/**
			 * 客户端异步请求达到Netty Buffer高水平线,阻流.
			 * **/
		    return false;
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
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponseBody msg) throws Exception {
		RpcClientTaskPool.addTask(msg); // 入池处理.
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.error("RPC客户端异常,message: " + message);
	}

}
