package raptor.core.server;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import raptor.core.Constants;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.init.RpcParameter;
import raptor.core.server.handler.ServerDispatcherHandler;
import raptor.exception.RpcException;
import raptor.util.StringUtil;

/**
 * @author gewx RPC Server
 **/
public final class RpcServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

	private static final ServerBootstrap server = new ServerBootstrap();

	private static final String ADDRESS_KEY = "localAddress";

	private static final String PORT = "port";
	
	private RpcServer() {
	}

	/**
	 * @author gewx 启动RPC服务
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
			EventLoopGroup acceptGroup = new EpollEventLoopGroup(1); 
			EventLoopGroup ioGroup = new EpollEventLoopGroup(Constants.CPU_CORE * 2); 
			server.group(acceptGroup, ioGroup).channel(EpollServerSocketChannel.class);
		} else {
			LOGGER.info("非Linux系统下,RPC Server启动...");
			EventLoopGroup acceptGroup = new NioEventLoopGroup(1); 
			EventLoopGroup ioGroup = new NioEventLoopGroup(Constants.CPU_CORE * 2); 
			server.group(acceptGroup, ioGroup).channel(NioServerSocketChannel.class);
		}
		
		InetSocketAddress localAddress = null;
		if (StringUtils.isNotBlank(serverConfig.get(ADDRESS_KEY))) {
			localAddress = new InetSocketAddress(serverConfig.get(ADDRESS_KEY),Integer.parseInt(serverConfig.get(PORT)));
		} else {
			localAddress = new InetSocketAddress(Integer.parseInt(serverConfig.get(PORT)));
		}
		
		/**
		 *  服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝。默认值，Windows为200，其他为128。
		 * **/
		server.option(ChannelOption.SO_BACKLOG, 8192) 
				.childOption(ChannelOption.SO_RCVBUF, 256 * Constants.ONE_KB)
				.childOption(ChannelOption.SO_SNDBUF, 256 * Constants.ONE_KB)
				.localAddress(localAddress)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipline = ch.pipeline();
						pipline.addLast(new RpcByteToMessageDecoder());
						pipline.addLast(new RpcMessageToByteEncoder());
						pipline.addLast(new ServerDispatcherHandler());
					}
				});

		server.bind().sync().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {					
					LOGGER.info("RPC 服务启动成功!");
				} else {
					String message = StringUtil.getErrorText(future.cause());
					LOGGER.warn("RPC 服务启动失败,message: " + message);
					throw new RpcException("RPC Server启动失败,message: " + message);
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
