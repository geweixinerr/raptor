package raptor.core.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.client.TestData;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.util.StringUtil;

/**
 * @author gewx 客户端入站处理器
 **/
public class ClientDispatcherHandler extends SimpleChannelInboundHandler<RpcResponseBody> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcherHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// 测试RPC调用.
		for (int i = 0; i < 10000; i++) {
			String message = "Netty RPC Send, Netty is VeryGood!";
			RpcRequestBody requestBody = new RpcRequestBody();
			requestBody.setBody(new Object[] { TestData.data, message });
			requestBody.setRpcMethod("LoginAuth");
			requestBody.setMessageId("MessageId-[" + i + "]");
			ctx.writeAndFlush(requestBody);
		}
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

}
