package raptor.core.server.handler;

import java.nio.charset.Charset;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author gewx RPC Server业务分发器
 * **/
public final class DispatcherHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("连接建立成功!");
		ctx.writeAndFlush(Unpooled.copiedBuffer("中国万岁!", Charset.forName("UTF-8")));
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
	}

	
}
