package raptor.core.pool;

import java.net.InetSocketAddress;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import raptor.core.Constants;
import raptor.core.RpcPushDefine;
import raptor.exception.RpcException;
import raptor.util.StringUtil;

/**
 * @author gewx tcp连接对象池
 **/
public final class TcpPoolFactory extends BasePooledObjectFactory<RpcPushDefine> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpPoolFactory.class);
	
	private final String remoteAddr;

	private final int port;
	
	private final String serverNode;
	
	private final Bootstrap bootStrap;
    
	public TcpPoolFactory(String remoteAddr, int port, String serverNode, Bootstrap bootStrap) {
		this.remoteAddr = remoteAddr;
		this.port = port;
		this.serverNode = serverNode;
		this.bootStrap = bootStrap;
	}

	@Override
	public RpcPushDefine create() {
		ChannelFuture future = null;
		try {
			future = bootStrap.connect(this.remoteAddr,this.port).sync(); 
			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						InetSocketAddress local = (InetSocketAddress) future.channel().localAddress();
						InetSocketAddress remote = (InetSocketAddress) future.channel().remoteAddress();
						LOGGER.info("客户端连接成功,localAddress: " + local.getAddress() + ":" + local.getPort()
								+ ", remoteAddress: " + remote.getAddress() + ":" + remote.getPort() +", serverNode: " + serverNode);
					} else {
						String message = StringUtil.getErrorText(future.cause());
						LOGGER.warn("tcp连接建立初始化异常-0,serverNode: " + serverNode +", message: " + message);
						throw new RpcException("tcp连接建立初始化异常-0,serverNode: " + serverNode +", message: " + message);
					}
				}
			});
		} catch (Exception e) {
			String message = StringUtil.getErrorText(e);
			LOGGER.error("tcp连接建立初始化异常-1,serverNode: " + serverNode +", message: " + message);
			throw new RpcException("tcp连接建立初始化异常-1,serverNode: " + serverNode +", message: " + message);
		}

		RpcPushDefine handler = (RpcPushDefine) future.channel().pipeline().get(Constants.CLIENT_DISPATCHER);
		return handler;
	}

	@Override
	public PooledObject<RpcPushDefine> wrap(RpcPushDefine channelHandler) {
		return new DefaultPooledObject<RpcPushDefine>(channelHandler);
	}

	@Override
	public void passivateObject(PooledObject<RpcPushDefine> pooledObject) {
		//对象入池操作
	}

	@Override
	public void destroyObject(PooledObject<RpcPushDefine> pooledObject) throws Exception {
		ObjectPool<RpcPushDefine> pool = pooledObject.getObject().getRpcPoolObject();
		LOGGER.info("资源入池,tcpId: " + pooledObject.getObject().getTcpId() +", active: " + pool.getNumActive() +", Idle: " + pool.getNumIdle());
		pooledObject.getObject().close();
	}
	
}
