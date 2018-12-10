package raptor.core.server;

import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import raptor.core.init.RpcParameter;
import raptor.core.server.handler.DispatcherHandler;
import raptor.util.StringUtil;

/**
 * @author gewx RPC Server
 **/
public final class RpcServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

	// CPU核心数
	private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

	private static final ServerBootstrap server = new ServerBootstrap();

	private static final String ADDRESS_KEY = "localAddress";

	private static final String PORT = "port";

	private RpcServer() {
	}

	/**
	 * 启动RPC服务
	 * 
	 * @throws InterruptedException
	 **/
	public static void start() throws InterruptedException {
		/**
		 * Reactor主从多线程模型 接受客户端请求的线程池,无需太多,默认设置为物理机核心CPU数. IO处理线程池,默认为物理机核心CPU数 * 2.
		 **/

		Map<String, String> serverConfig = RpcParameter.INSTANCE.getServerConfig();
		if (SystemUtils.IS_OS_LINUX) {
			LOGGER.info("Linux系统下,RPC Server启动...");
			// Linux Epoll
			EventLoopGroup acceptGroup = new EpollEventLoopGroup(CPU_CORE);
			EventLoopGroup ioGroup = new EpollEventLoopGroup(CPU_CORE * 2);
			server.group(acceptGroup, ioGroup).channel(EpollServerSocketChannel.class);
		} else {
			LOGGER.info("非Linux系统下,RPC Server启动...");
			EventLoopGroup acceptGroup = new NioEventLoopGroup(CPU_CORE);
			EventLoopGroup ioGroup = new NioEventLoopGroup(CPU_CORE * 2);
			server.group(acceptGroup, ioGroup).channel(NioServerSocketChannel.class);
		}

		server.option(ChannelOption.SO_BACKLOG, 100)
				.localAddress(serverConfig.get(ADDRESS_KEY), Integer.parseInt(serverConfig.get(PORT)))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipline = ch.pipeline();
						pipline.addLast(new LoggingHandler(LogLevel.INFO)); //开启日志监控
						pipline.addLast(new LineBasedFrameDecoder(1024));
						pipline.addLast(new StringDecoder());
						pipline.addLast(new DispatcherHandler());
					}
				});

		// 同步绑定
		server.bind().sync().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					LOGGER.info("RPC 服务启动成功!");
				} else {
					String message = StringUtil.getErrorText(future.cause());
					LOGGER.info("RPC 服务启动失败,message: " + message);
				}
			}
		});

	}

	/**
	 * @author gewx 释放资源
	 **/
	public static void stop() {
		server.config().group().shutdownGracefully();
		server.config().childGroup().shutdownGracefully();
	}

}
