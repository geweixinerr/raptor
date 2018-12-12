package raptor.core.client.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.core.server.handler.ServerDispatcherHandler;
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
			Map<String, String> params = new HashMap<String, String>();
			for (int j = 0; j < 1000; j++) {
				params.put("Message" + i,
						"1. 概念\r\n" + 
						"Java NIO API自带的缓冲区类功能相当有限，没有经过优化，使用JDK的ByteBuffer操作更复杂。故而Netty的作者Trustin Lee为了实现高效率的网络传输，重新造轮子，Netty中的ByteBuf实际上就相当于JDK中的ByteBuffer，其作用是在Netty中通过Channel传输数据。\r\n" + 
						"\r\n" + 
						"2. 优势\r\n" + 
						"可以自定义缓冲类型；\r\n" + 
						"通过内置的复合缓冲类型，实现透明的零拷贝（zero-copy）；\r\n" + 
						"不需要调用flip()来切换读/写模式；\r\n" + 
						"读取和写入索引分开；\r\n" + 
						"方法链；\r\n" + 
						"引用计数；\r\n" + 
						"Pooling（池）。");
			}
			String message = "Netty RPC Send, Netty is VeryGood!";
			RpcRequestBody requestBody = new RpcRequestBody();
			requestBody.setBody(new Object[] { params, message });
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
