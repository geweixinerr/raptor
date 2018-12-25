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
		LOGGER.info("应用服务器启动,RPC客户端/服务端参数初始化...");
		List<Map<String,String>> clientConfig = (List<Map<String,String>>) context.getBean("NettyPoolConfig");
		Map<String,String> serverConfig = (Map<String,String>) context.getBean("RpcServerConfig");
		
		RpcParameter.INSTANCE.initRpcParameter(clientConfig, serverConfig); //init参数

		//step1.初始化建立服务端RPC映射关系
		Map<String,Object> rpcMap = context.getBeansWithAnnotation(RpcHandler.class);
		RpcMappingInit.initRpcMapping(rpcMap);
		
		//step2.启动/初始化业务线程池.
		RpcServerTaskPool.initPool();
		RpcClientTaskPool.initPool();
		
		//step3.启动客户端超时请求清理器
		RpcClientTimeOutScan.scan();
		
		//step4.启动RPC服务服务端
		RpcServer.start();		
		
		//step5. 启动RPC服务客户端
		RpcClient.start();
	}
	
	@Override
	public void destroy() throws Exception {
		LOGGER.info("应用服务器关闭,释放RPC资源");
		RpcServer.stop();
	}
	
}