package raptor.test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClientRegistry;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.client.task.RpcClientTimeOutScan;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.message.RpcResponseBody;
import raptor.util.StringUtil;

/**
 * @author gewx Netty客户端测试类.
 **/
@SuppressWarnings("unused")
public final class TestRpcClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestRpcClient.class);

	private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

	// 客户端分发器注册pipline Key
	private static final String CLIENT_DISPATCHER = "clientDispatcher";

	static {
		try {
			TestRpcClient.start();
		} catch (InterruptedException e) {
			System.out.println("服务启动失败,Message: " + e.getMessage());
		}

		RpcClientTimeOutScan.scan();
	}

	private TestRpcClient() {

	}

	private static void init() {
		RpcPushDefine rpcClient = null;
		while ((rpcClient = RpcClientRegistry.INSTANCE.getRpcClient(RpcClientRegistry.rpcEnum.rpcPushDefine)) == null) {
			if (rpcClient != null) {
				break;
			}
		}
		TestRaptorRpc.rpcClient = rpcClient; // init
	}

	/**
	 * 启动客户端连接
	 * 
	 * @throws InterruptedException
	 **/
	public static void start() throws InterruptedException {

		Bootstrap boot = new Bootstrap();
		EventLoopGroup eventGroup = new NioEventLoopGroup(CPU_CORE * 3);
		boot.group(eventGroup).channel(NioSocketChannel.class).remoteAddress("127.0.0.1", 8090)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).option(ChannelOption.SO_SNDBUF, 128 * 1024) // 设置发送缓冲大小
				.option(ChannelOption.SO_RCVBUF, 128 * 1024) // 设置接收缓冲大小
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		init();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("userName", "geweixin");

		TestRaptorRpc rpc = new TestRaptorRpc();

		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		NettyTestData data = new NettyTestData();

		 
		Executor execute = Executors.newFixedThreadPool(CPU_CORE * 2);
		CyclicBarrier latch = new CyclicBarrier(CPU_CORE * 2);

		for (int i = 0; i < CPU_CORE * 2; i++) {
			execute.execute(new Runnable() {
				@Override
				public void run() {
					try {
						latch.await();
						// 发送异步消息.
						for (int j = 0; j < 10000; j++) {
							rpc.sendAsyncMessage("remote", "LoginAuth", new AbstractCallBack() {
								@Override
								public void invoke(RpcResponseBody responseBody) {
									System.out.println("请求结果: " + responseBody.getSuccess() + ", Message: "
											+ responseBody.getMessage() + ", Result: " + responseBody.getBody());
								}
							}, 5, data, message);
						}
					} catch (InterruptedException | BrokenBarrierException e) {

					}
				}
			});
		}
		
		/*
		for (int j = 0; j < 10000; j++) {
			rpc.sendAsyncMessage("remote", "LoginAuth", new AbstractCallBack() {
				@Override
				public void invoke(RpcResponseBody responseBody) {
					System.out.println("请求结果: " + responseBody.getSuccess() + ", Message: "
							+ responseBody.getMessage() + ", Result: " + responseBody.getBody());
				}
			}, 5, data, message);
		}
		*/
		
	}
}
