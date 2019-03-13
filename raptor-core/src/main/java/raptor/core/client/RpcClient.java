package raptor.core.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eaio.uuid.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import raptor.core.Constants;
import raptor.core.RpcPushDefine;
import raptor.core.client.handler.ClientDispatcherHandler;
import raptor.core.handler.codec.RpcByteToMessageDecoder;
import raptor.core.handler.codec.RpcMessageToByteEncoder;
import raptor.core.init.RpcParameter;
import raptor.core.pool.TcpPoolFactory;

/**
 * @author gewx Netty客户端类
 **/
public final class RpcClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

	/**
	 * @author gewx 多客户端配置与连接池映射关系.
	 * **/
	private static final Map<String,ObjectPool<RpcPushDefine>> RPC_OBJECT_POOL = new ConcurrentHashMap<String,ObjectPool<RpcPushDefine>>();
	
	/**
	 * 默认连接超时时间
	 * **/
	private static final Integer DEFAULT_TIME_OUT = 5000;
	
	/**
	 * 远程服务器地址配置key
	 * **/
	private static final String REMOTE_ADDR = "remote";
	
	/**
	 * 远程服务器地址端口配置key
	 * **/
	private static final String PORT = "port";
	
	/**
	 * 服务器节点标识
	 * **/
	private static final String SERVER_NODE = "serverNode";
	
	/**
	 * 最大连接数
	 * **/
	private static final String MAX_CLIENTS = "maxclients";

	/**
	 * 最大连接数-默认值
	 * **/
	private static final Integer DEFAULT_MAX_CLIENTS = 1024;
	
	/**
	 * 最小连接数
	 * **/
	private static final String MIN_CLIENTS = "minclients";
	
	/**
	 * 最小连接数-默认值
	 * **/
	private static final Integer DEFAULT_MIN_CLIENTS = 6;
	
	/**
	 * 时间设置常量,默认单位1000毫秒.
	 * **/
	private static final Integer DEFAULT_MILLIS = 1000;

	private RpcClient() {

	}

	/**
	 * @author gewx 启动客户端连接
	 * @throws Exception
	 **/
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public static void connection() throws Exception {
		Bootstrap boot = new Bootstrap();
		EventLoopGroup eventGroup = new NioEventLoopGroup(Constants.CPU_CORE + 1);
		boot.group(eventGroup).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIME_OUT) 
				.option(ChannelOption.SO_RCVBUF, 256 * Constants.ONE_KB) 
				.option(ChannelOption.SO_SNDBUF, 256 * Constants.ONE_KB);
		
		List<Map<String, String>> clientConfig = RpcParameter.INSTANCE.getClientConfig();
		for (Map<String,String> en : clientConfig) {
	    	String maxclients = ObjectUtils.defaultIfNull(en.get(MAX_CLIENTS),String.valueOf(DEFAULT_MAX_CLIENTS));
	    	String minclients = ObjectUtils.defaultIfNull(en.get(MIN_CLIENTS),String.valueOf(DEFAULT_MIN_CLIENTS));
	    	String serverNode = en.get(SERVER_NODE);
	    	
	    	//对象池配置
	    	GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
	    	conf.setLifo(true);
	    	conf.setMaxTotal(Integer.parseInt(maxclients));  //池中最多可用的实例个数
	    	conf.setMaxIdle(Integer.parseInt(maxclients)); //连接池中最大空闲的连接数,默认为8
	    	conf.setMinIdle(Integer.parseInt(minclients)); //连接池中最少空闲的连接数,默认为0
	    	conf.setBlockWhenExhausted(true); //池无可用对象,是否堵塞等待对象创建. (true:等待,false:不等待)
	    	conf.setMaxWaitMillis(-1); //调用borrowObject方法时，需要等待的最长时间. 单位:毫秒
	    	conf.setMinEvictableIdleTimeMillis(-1); //连接空闲的最小时间，达到此值后空闲连接将可能会被移除 （-1 :不移除,使用setSoftMinEvictableIdleTimeMillis配置）
	    	conf.setSoftMinEvictableIdleTimeMillis(5 * 60 * DEFAULT_MILLIS); //连接空闲的最小时间，达到此值后空闲连接将可能会被移除[tcp连接空闲超时设置5分钟]
	    	conf.setTimeBetweenEvictionRunsMillis(10 * DEFAULT_MILLIS); //闲置实例校验器启动的时间间隔,单位是毫秒 [10秒扫描一次]

	    	boot.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipline = ch.pipeline();
					pipline.addLast(new IdleStateHandler(0, 60 * 5, 0, TimeUnit.SECONDS)); //心跳检测5分钟[单个tcp连接5分钟内没有出站/入站动作]
					pipline.addLast(new RpcByteToMessageDecoder());
					pipline.addLast(new RpcMessageToByteEncoder());
					pipline.addLast(Constants.CLIENT_DISPATCHER, new ClientDispatcherHandler(new UUID().toString(), serverNode));
				}
			});
			
	    	PooledObjectFactory poolFactory = new TcpPoolFactory(en.get(REMOTE_ADDR),Integer.parseInt(en.get(PORT)),serverNode,boot);
	    	ObjectPool<RpcPushDefine> pool = new GenericObjectPool<RpcPushDefine>(poolFactory,conf);

	    	RPC_OBJECT_POOL.put(en.get(SERVER_NODE), pool);
	    		    	
	        LOGGER.info("初始化客户端:" + en.get(SERVER_NODE) + ", tcp连接池最小连接数: " + minclients);
	    	int num = Integer.parseInt(minclients);	    	
	    	for (int i = 0; i < num; i++) {
				pool.addObject();
	    	}
		}
	}
	
	/**
	 * @author gewx 返回映射关系
	 * **/
	public static Map<String,ObjectPool<RpcPushDefine>> getRpcPoolMapping() {
		return Collections.unmodifiableMap(RPC_OBJECT_POOL);
	}
}
