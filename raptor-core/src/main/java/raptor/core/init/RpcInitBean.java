package raptor.core.init;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import raptor.core.annotation.RpcHandler;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.client.task.RpcClientTimeOutScan;
import raptor.core.server.RpcServer;
import raptor.core.server.RpcServerTaskPool;

/**
 * @author gewx RPC客户端/服务端初始化,并负责RPC服务关闭资源释放
 * **/

@Service
public final class RpcInitBean implements ApplicationContextAware , InitializingBean , DisposableBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcInitBean.class);
		
	private ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {	
		this.context = applicationContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		//服务器启动
		if (context.containsBean("RpcServerConfig")) {
			LOGGER.info("应用服务器启动,RPC服务端参数初始化...");
			Map<String,String> serverConfig = (Map<String,String>) context.getBean("RpcServerConfig");
			RpcParameter.INSTANCE.initRpcParameter(serverConfig);
			RpcServerTaskPool.initPool();
			RpcServer.start();	
		}
		
		//客户端启动
		if (context.containsBean("NettyPoolConfig")) {
			LOGGER.info("应用服务器启动,RPC客户端参数初始化...");
			List<Map<String,String>> clientConfig = (List<Map<String,String>>) context.getBean("NettyPoolConfig");
			RpcParameter.INSTANCE.initRpcParameter(clientConfig);		
			RpcClientTaskPool.initPool();	
			RpcClientTimeOutScan.scan(); //启动客户端超时请求清理器
			RpcClient.start();
		}
	
		//初始化建立服务端RPC映射关系
		Map<String,Object> rpcMap = context.getBeansWithAnnotation(RpcHandler.class);
		RpcMappingInit.initRpcMapping(rpcMap);
	}
	
	@Override
	public void destroy() throws Exception {
		LOGGER.info("应用服务器关闭,释放RPC资源");
		RpcServer.stop();
	}
	
}