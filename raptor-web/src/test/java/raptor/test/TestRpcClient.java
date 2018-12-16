package raptor.test;

import java.net.InetSocketAddress;

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
import raptor.core.AbstractCallBack;
import raptor.core.RpcPushDefine;
import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClientRegistry;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.server.RpcResult;
import raptor.util.StringUtil;

/**
 * @author gewx Netty客户端测试类.
 **/
@SuppressWarnings("unused")
public final class TestRpcClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestRpcClient.class);

	// 客户端分发器注册pipline Key
	private static final String CLIENT_DISPATCHER = "clientDispatcher";

	static {
		try {
			TestRpcClient.start();
		} catch (InterruptedException e) {
			System.out.println("服务启动失败,Message: " + e.getMessage());
		}
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
		EventLoopGroup eventGroup = new NioEventLoopGroup();
		boot.group(eventGroup).channel(NioSocketChannel.class).remoteAddress("127.0.0.1", 8090)
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

		TestRaptorRpc rpc = new TestRaptorRpc();

		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		NettyTestData data = new NettyTestData();
		// 发送异步消息.
		rpc.sendAsyncMessage("remote", "LoginAuth", new AbstractCallBack() {
			@Override
			public void invoke(RpcResult result) {
				System.out.println("请求结果: " + result);
			}
		}, data, message);

	}
}
