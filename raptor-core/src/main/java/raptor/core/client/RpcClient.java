package raptor.core.client;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.init.RpcParameter;
import raptor.util.StringUtil;

/**
 * @author gewx Netty客户端
 **/
public final class RpcClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

	// 客户端分发器注册pipline Key
	private static final String CLIENT_DISPATCHER = "clientDispatcher";

	private RpcClient() {

	}

	/**
	 * 启动客户端连接
	 * 
	 * @throws InterruptedException
	 **/
	public static void start() throws InterruptedException {
		Map<String, String[]> clientConfig = RpcParameter.INSTANCE.getClientConfig(); // 客户端配置参数

		Bootstrap boot = new Bootstrap();
		EventLoopGroup eventGroup = new NioEventLoopGroup();
		boot.group(eventGroup).channel(NioSocketChannel.class)
				.remoteAddress(clientConfig.get("remote")[0], Integer.parseInt(clientConfig.get("remote")[1])) //TODO 先固定写死。
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipline = ch.pipeline();
						pipline.addLast(new RpcByteToMessageDecoder());
						pipline.addLast(new RpcMessageToByteEncoder());
						pipline.addLast(CLIENT_DISPATCHER, new ClientDispatcherHandler());
					}
				});

		ChannelFuture future = boot.connect().sync();
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					InetSocketAddress local = (InetSocketAddress) future.channel().localAddress();
					InetSocketAddress remote = (InetSocketAddress) future.channel().remoteAddress();
					LOGGER.info("客户端连接成功,localAddress: " + local.getAddress() + ":" + local.getPort()
							+ ", remoteAddress: " + remote.getAddress() + ":" + remote.getPort());
					ClientDispatcherHandler handler = (ClientDispatcherHandler) future.channel().pipeline()
							.get(CLIENT_DISPATCHER);
					RpcClientRegistry.INSTANCE.registry(RpcClientRegistry.rpcEnum.rpcPushDefine, handler);
					LOGGER.info("客户端服务注册成功.");
				} else {
					LOGGER.info("客户端连接失败,message: " + StringUtil.getErrorText(future.cause()));
				}
			}
		});
	}
}
