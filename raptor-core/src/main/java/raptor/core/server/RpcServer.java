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
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import raptor.core.Constants;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.init.RpcParameter;
import raptor.core.init.RpcParameterEnum;
import raptor.core.server.handler.ServerDispatcherHandler;
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

	/**
	 * TCP参数配置
	 * **/
	private static final Integer ONE_KB = 1024; //1KB数值常量.
	
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
			EventLoopGroup acceptGroup = new EpollEventLoopGroup(1); // accept connection thread ,1个足矣.
			EventLoopGroup ioGroup = new EpollEventLoopGroup(CPU_CORE * 2); //网络IO处理线程池
			server.group(acceptGroup, ioGroup).channel(EpollServerSocketChannel.class);
		} else {
			LOGGER.info("非Linux系统下,RPC Server启动...");
			EventLoopGroup acceptGroup = new NioEventLoopGroup(1); // accept connection thread ,1个足矣.
			EventLoopGroup ioGroup = new NioEventLoopGroup(CPU_CORE * 2); //网络IO处理线程池
			server.group(acceptGroup, ioGroup).channel(NioServerSocketChannel.class);
		}
		
		
		server.option(ChannelOption.SO_BACKLOG, 1024) // 服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝。默认值，Windows为200，其他为128。
				.childOption(ChannelOption.SO_RCVBUF, 14 * ONE_KB * ONE_KB) // 接受窗口(window size value),设置为512kb
				.childOption(ChannelOption.SO_SNDBUF, 10 * ONE_KB * ONE_KB) // 发送窗口(window size value),设置为256kb
				.childOption(ChannelOption.TCP_NODELAY, false) //启用/禁用 TCP_NODELAY（启用/禁用 Nagle 算法）。
				/**
				 * 基于socket buffer设置Netty Buffer.  高水平线为socket buffer的 80% ,低水平线为高位水平线的1/2.
				 * **/
				.localAddress(serverConfig.get(ADDRESS_KEY), Integer.parseInt(serverConfig.get(PORT)))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						String logOnOff = serverConfig.get(RpcParameterEnum.LOGONOFF.getCode());
						ChannelPipeline pipline = ch.pipeline();
						if (Constants.LogOn.equals(logOnOff)) {
							pipline.addLast(new LoggingHandler(LogLevel.INFO)); // 开启日志监控
						}
						pipline.addLast(new RpcByteToMessageDecoder());
						pipline.addLast(new RpcMessageToByteEncoder());
						pipline.addLast(new ServerDispatcherHandler());
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
