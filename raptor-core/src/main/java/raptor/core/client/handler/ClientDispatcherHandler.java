package raptor.core.client.handler;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import raptor.core.PushMessageCallBack;
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
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
	}

	/**
	 * @author gewx 消息推送
	 **/
	@Override
	public void pushMessage(RpcRequestBody requestBody, PushMessageCallBack call) {
		ChannelFuture future = ctx.writeAndFlush(requestBody);
		try {
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
					   //放入数据推送结果,推送失败，客户端可以直接获取到推送失败结果.
					} else {
						System.out.println("数据推送失败,message: " + StringUtil.getErrorText(future.cause()));
//						System.exit(0);
					}
				}
			});
		} finally {
			call.invoke(); //回调,tcp连接资源入池.
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
	public boolean isWritable() {
		return ctx.channel().isWritable();
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
