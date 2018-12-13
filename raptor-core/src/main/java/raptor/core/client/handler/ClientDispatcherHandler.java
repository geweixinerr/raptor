package raptor.core.client.handler;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.util.StringUtil;

/**
 * @author gewx 客户端入站处理器
 **/
@ThreadSafe
@Sharable
public class ClientDispatcherHandler extends SimpleChannelInboundHandler<RpcResponseBody> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcherHandler.class);

	private ChannelHandlerContext ctx;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponseBody msg) throws Exception {
		RpcResponseBody body = (RpcResponseBody) msg;
		System.out.println("RPC客户端响应: " + body);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.warn("RPC客户端异常,message: " + message);
	}
	
	/**
	 * @author gewx 消息推送服务.
	 * **/
	public void pushMessage(RpcRequestBody requestBody) {
		ChannelFuture channelfuture = ctx.writeAndFlush(requestBody);
		channelfuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					LOGGER.info("RPC 数据发送成功.");
				} else {
					LOGGER.info("RPC 数据发送失败, message : " + StringUtil.getErrorText(future.cause()));
				}
			}
		});
	}

}
