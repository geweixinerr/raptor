package raptor.core.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.AbstractCallBack;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.core.server.RpcServerTaskPool;
import raptor.log.RaptorLogger;
import raptor.util.StringUtil;

/**
 * @author gewx RPC Server业务分发器
 **/
public final class ServerDispatcherHandler extends SimpleChannelInboundHandler<RpcRequestBody> {

	private static final RaptorLogger LOGGER = new RaptorLogger(ServerDispatcherHandler.class);

	private static final Logger RAW_LOGGER = LoggerFactory.getLogger(ServerDispatcherHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequestBody msg) throws Exception {
		RpcServerTaskPool.addTask(msg, new AbstractCallBack() {
			@Override
			public void invoke(RpcResponseBody responseBody) {
				ChannelFuture future = ctx.writeAndFlush(responseBody);
				future.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							LOGGER.info("RPC服务端数据出站SUCCESS, " + responseBody);
						} else {
							String message = StringUtil.getErrorText(future.cause());
							LOGGER.warn("RPC服务端数据出站FAIL: " + responseBody + ", message: " + message);	
						}
					}
				});
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		RAW_LOGGER.warn("RPC服务端异常,message: " + message);
	}
	
}
