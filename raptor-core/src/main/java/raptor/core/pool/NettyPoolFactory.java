package raptor.core.pool;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import raptor.core.RpcPushDefine;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.exception.RpcException;
import raptor.util.StringUtil;

/**
 * @author gewx tcp连接对象池
 **/
public final class NettyPoolFactory extends BasePooledObjectFactory<RpcPushDefine> {

	private static final Logger LOGGER = LoggerFactory.getLogger(NettyPoolFactory.class);

	// CPU核心数
	private static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();

	// 客户端分发器注册pipline Key
	private static final String CLIENT_DISPATCHER = "clientDispatcher";

	// 默认超时设置,5秒
	private static final Integer DEFAULT_TIME_OUT = 5000;

    //字节数值空间常量
	private static final Integer ONE_KB = 1024; // 1KB数值常量.
	
	/**
	 * 远程服务器地址
	 **/
	private final String remoteAddr;

	/**
	 * 远程服务器端口
	 **/
	private final int port;
	
	/**
	 * 当前poolFactory 隶属于哪个server节点
	 * **/
    private final String serverNode;
	
    /**
     * 速率
     * **/
    private final Integer speedNum;
    
	public NettyPoolFactory(String remoteAddr, int port, String serverNode, Integer speedNum) {
		this.remoteAddr = remoteAddr;
		this.port = port;
		this.serverNode = serverNode;
		this.speedNum = speedNum;
	}

	@Override
	public RpcPushDefine create() {
		String serverNode = this.serverNode; //服务器节点
		Integer speedNum = this.speedNum; //速率 
		
		Bootstrap boot = new Bootstrap();
		EventLoopGroup eventGroup = new NioEventLoopGroup(CPU_CORE * 2);// 网络IO处理线程池
		boot.group(eventGroup).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIME_OUT) // 设置连接超时5秒,默认值30000毫秒即30秒。
				.option(ChannelOption.SO_RCVBUF, 128 * ONE_KB) // 接受窗口(window size value),设置为128kb
				.option(ChannelOption.SO_SNDBUF, 128 * ONE_KB) // 发送窗口(window size value),设置为128kb
				// 默认WriteBufferWaterMark(low: 32768, high: 65536)
				//Netty Buffer高水平线,限流使用.
				.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * ONE_KB, 16 * ONE_KB))
				.remoteAddress(remoteAddr, port).handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline pipline = ch.pipeline();
						pipline.addLast(new IdleStateHandler(0,60 * 2,0, TimeUnit.SECONDS)); //心跳检测2分钟[单个tcp连接2分钟内没有出站动作]
						pipline.addLast(new RpcByteToMessageDecoder());
						pipline.addLast(new RpcMessageToByteEncoder());
						pipline.addLast(CLIENT_DISPATCHER, new ClientDispatcherHandler(new UUID().toString(), serverNode, speedNum));
					}
				});

		ChannelFuture future = null;
		try {
			future = boot.connect().sync(); //同步连接.
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						InetSocketAddress local = (InetSocketAddress) future.channel().localAddress();
						InetSocketAddress remote = (InetSocketAddress) future.channel().remoteAddress();
						LOGGER.info("客户端连接成功,localAddress: " + local.getAddress() + ":" + local.getPort()
								+ ", remoteAddress: " + remote.getAddress() + ":" + remote.getPort());
						LOGGER.info("客户端服务注册成功.");
					} else {
						String message = StringUtil.getErrorText(future.cause());
						LOGGER.warn("tcp连接建立初始化异常-0,message: " + message);
						throw new RpcException("tcp连接建立初始化异常-0,message: " + message);
					}
				}
			});
		} catch (Exception e) {
			String message = StringUtil.getErrorText(e);
			LOGGER.error("tcp连接建立初始化异常-1,message: " + message);
			throw new RpcException("tcp连接建立初始化异常-1,message: " + message);
		}

		RpcPushDefine handler = (RpcPushDefine) future.channel().pipeline().get(CLIENT_DISPATCHER);
		return handler;
	}

	/**
	 * Use the default PooledObject implementation.
	 */
	@Override
	public PooledObject<RpcPushDefine> wrap(RpcPushDefine channelHandler) {
		return new DefaultPooledObject<RpcPushDefine>(channelHandler);
	}

	/**
	 * When an object is returned to the pool, clear the buffer.
	 */
	@Override
	public void passivateObject(PooledObject<RpcPushDefine> pooledObject) {
		// 钝化,不处理. 对象返回池中时的动作.
//		LOGGER.info("资源入池...,tcpId: " + pooledObject.getObject().getTcpId());
	}

	/**
	 * 销毁对象,关闭tcp连接
	 * **/
	@Override
	public void destroyObject(PooledObject<RpcPushDefine> p) throws Exception {
		p.getObject().close();
	}
	
}
