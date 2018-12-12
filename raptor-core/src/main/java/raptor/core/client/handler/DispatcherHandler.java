package raptor.core.client.handler;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;

/**
 * @author gewx 客户端入站处理器
 * **/
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//测试RPC调用.
		Map<String,String> params = new HashMap<String,String>();
		params.put("authId", "ABCDEFG");
		String message = "葛伟新!";
		RpcRequestBody requestBody = new RpcRequestBody();
		requestBody.setBody(new Object[] {params});
		requestBody.setRpcMethod("LoginAuth");
		requestBody.setMessageId("ABCDEFG_MESSAGE");
		ctx.writeAndFlush(requestBody);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		RpcResponseBody body = (RpcResponseBody) msg;
		System.out.println("响应消息: " + body);
	}

}
