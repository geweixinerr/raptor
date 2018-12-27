package raptor.core.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raptor.core.RpcPushDefine;
import raptor.core.init.RpcParameter;
import raptor.core.pool.NettyPoolFactory;

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
	 * 远程服务器地址配置key
	 * **/
	private static final String REMOTE_ADDR = "remote";
	
	/**
	 * 远程服务器地址端口配置key
	 * **/
	private static final String PORT = "port";
	
	/**
	 * 客户端标识
	 * **/
	private static final String CLIENT_NAME = "clientName";
	
	/**
	 * 最大连接数
	 * **/
	private static final String MAX_CLIENTS = "maxclients";
	

	/**
	 * 最大连接数-默认值
	 * **/
	private static final Integer DEFAULT_MAX_CLIENTS = 12040;
	
	/**
	 * 最小连接数
	 * **/
	private static final String MIN_CLIENTS = "minclients";
	
	/**
	 * 最小连接数-默认值
	 * **/
	private static final Integer DEFAULT_MIN_CLIENTS = 6;
	
	/**
	 * 从池中获取对象,最大等待时间 MAXWAIT_MILLIS,默认1000毫秒
	 * **/
	private static final Integer DEFAULT_MAXWAIT_MILLIS = 1000;
	

	private RpcClient() {

	}

	/**
	 * 启动客户端连接
	 * 
	 * @throws InterruptedException
	 **/
	@SuppressWarnings({ "rawtypes", "unchecked"})
	public static void start() throws Exception {
		List<Map<String, String>> clientConfig = RpcParameter.INSTANCE.getClientConfig(); // 客户端配置参数
		for (Map<String,String> en : clientConfig) {
	    	PooledObjectFactory poolFactory = new NettyPoolFactory(en.get(REMOTE_ADDR),Integer.parseInt(en.get(PORT)));
	    	
	    	//最大连接数
	    	String maxclients = ObjectUtils.defaultIfNull(en.get(MAX_CLIENTS),String.valueOf(DEFAULT_MAX_CLIENTS));
	    	//最小连接数
	    	String minclients = ObjectUtils.defaultIfNull(en.get(MIN_CLIENTS),String.valueOf(DEFAULT_MIN_CLIENTS));
	    	//对象池配置
	    	GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
	    	conf.setLifo(false); //池中实例的操作是否按照LIFO（后进先出）的原则,默认true[先入池的TCP连接先出]
	    	conf.setMaxTotal(Integer.parseInt(maxclients));  //池中最多可用的实例个数
	    	conf.setMaxIdle(Integer.parseInt(maxclients)); //连接池中最大空闲的连接数,默认为8
	    	conf.setMinIdle(Integer.parseInt(minclients)); //连接池中最少空闲的连接数,默认为0
	    	conf.setBlockWhenExhausted(false); //是否堵塞等待连接创建. (true:等待,false:不等待)
	    	conf.setMaxWaitMillis(DEFAULT_MAXWAIT_MILLIS); //调用borrowObject方法时，需要等待的最长时间. 单位:毫秒
	    	conf.setMinEvictableIdleTimeMillis(-1); //连接空闲的最小时间，达到此值后空闲连接将可能会被移除 （-1 :不移除,使用setSoftMinEvictableIdleTimeMillis配置）
	    	conf.setSoftMinEvictableIdleTimeMillis(1000 * 60 * 5); //连接空闲的最小时间，达到此值后空闲连接将可能会被移除
	    	conf.setTimeBetweenEvictionRunsMillis(8000); //闲置实例校验器启动的时间间隔,单位是毫秒
	    	ObjectPool<RpcPushDefine> pool = new GenericObjectPool<RpcPushDefine>(poolFactory,conf);

	    	RPC_OBJECT_POOL.put(en.get(CLIENT_NAME), pool); //入池
	    		    	
	    	/**
	    	 * init 初始化
	    	 * **/
	        LOGGER.info("初始化客户端:" + en.get(CLIENT_NAME) +", tcp连接池最小连接数: " + minclients);
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
