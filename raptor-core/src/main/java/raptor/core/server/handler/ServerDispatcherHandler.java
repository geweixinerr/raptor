package raptor.core.server.handler;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.AbstractCallBack;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.core.server.RpcServerTaskPool;
import raptor.util.StringUtil;

/**
 * @author gewx RPC Server业务分发器
 **/
public final class ServerDispatcherHandler extends SimpleChannelInboundHandler<RpcRequestBody> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerDispatcherHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequestBody msg) throws Exception {
		msg.setResponseTime(new DateTime());
		LOGGER.info("服务端收到信息: " + msg);
		/**
		 * 业务请求入池,与IO线程池隔离.执行完毕回调.
		 * **/
		RpcServerTaskPool.addTask(msg, new AbstractCallBack() {
			@Override
			public void invoke(RpcResponseBody responseBody) {
				//MDC.put("invokeId", "invokeId-"+java.util.UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
				ctx.writeAndFlush(responseBody);
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//处理异常,服务请求无损转发/超时(断线重连)等...
		String message = StringUtil.getErrorText(cause);
		LOGGER.warn("RPC服务端异常,message: " + message);
	}
	
}
