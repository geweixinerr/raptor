package raptor.core.server.handler;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.AbstractCallBack;
import raptor.core.server.RpcResult;
import raptor.core.server.RpcServerTaskPool;
import raptor.util.StringUtil;

/**
 * @author gewx RPC Server业务分发器
 **/
public final class DispatcherHandler extends SimpleChannelInboundHandler<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		RpcServerTaskPool.addTask(null, new AbstractCallBack() {
			@Override
			public void invoke(RpcResult result) {
				LOGGER.info("RPC执行结果,Result: " + result);
				ctx.writeAndFlush(Unpooled.copiedBuffer("Hello,RPC,回调!" + System.getProperty("line.separator"),
						Charset.defaultCharset()));
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
