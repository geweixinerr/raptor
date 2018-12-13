package raptor.core.client;

import java.util.Map;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import raptor.core.Constants;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.init.RpcParameter;
import raptor.core.init.RpcParameterEnum;

/**
 * @author gewx Netty客户端
 **/
public final class RpcClient {

	/**
	 * 启动客户端连接
	 * 
	 * @throws InterruptedException
	 **/
	public static void start() throws InterruptedException {
//		Map<String,String> clientConfig = RpcParameter.INSTANCE.getClientConfig(); //客户端配置参数

		EventLoopGroup eventGroup = new NioEventLoopGroup();
		Bootstrap boot = new Bootstrap();
		
		try {

			boot.group(eventGroup).channel(NioSocketChannel.class)
			        .remoteAddress("localhost", 8090).handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							/*
							String logOnOff  = clientConfig.get(RpcParameterEnum.LOGONOFF.getCode());
							ChannelPipeline pipline = ch.pipeline();
							if (Constants.LogOn.equals(logOnOff)) {
								pipline.addLast(new LoggingHandler(LogLevel.INFO)); // 开启日志监控
							}
							*/
							ChannelPipeline pipline = ch.pipeline();
							pipline.addLast(new RpcByteToMessageDecoder());
							pipline.addLast(new RpcMessageToByteEncoder());
							pipline.addLast(new ClientDispatcherHandler());
						}
					});

			ChannelFuture future = boot.connect().sync();
			future.channel().closeFuture().sync();

		} finally {
			eventGroup.shutdownGracefully().sync();
		}
	}

	public static void main(String[] args) throws Exception {
		RpcClient.start();
	}
}
