package raptor.core.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import raptor.util.StringUtil;

/**
 * @author gewx 入站异常处理Handler
 * **/
public final class ExceptionHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String message = StringUtil.getErrorText(cause);
		LOGGER.warn("RPC服务端异常,message: " + message);
	}

}
