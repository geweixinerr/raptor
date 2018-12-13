package raptor;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;

/**
 * @author gewx Netty客户端
 **/
public final class RpcClientT {

	/**
	 * 启动客户端连接
	 * 
	 * @throws InterruptedException
	 **/
	public static void start() throws InterruptedException {

		EventLoopGroup eventGroup = new NioEventLoopGroup();
		Bootstrap boot = new Bootstrap();
		
		try {

			boot.group(eventGroup).channel(NioSocketChannel.class)
			        .remoteAddress("localhost", 8090).handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
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
		RpcClientT.start();
	}
}
