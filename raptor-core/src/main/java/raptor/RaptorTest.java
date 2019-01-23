package raptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ResourceUtils;

import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.ext.spring.LogbackConfigurer;
import raptor.core.AbstractCallBack;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.client.task.RpcClientTimeOutScan;
import raptor.core.init.RpcParameter;
import raptor.core.message.RpcResponseBody;
import raptor.log.RaptorLogger;

/**
 * Raptor单元测试类
 * **/
public final class RaptorTest {
	
	private static final RaptorLogger LOGGER = new RaptorLogger(RaptorTest.class);

	static {
		//init logback
		try {
			File file = ResourceUtils.getFile("classpath:raptorLogback.xml");
			LogbackConfigurer.initLogging(file.toURI().toString());
		} catch (FileNotFoundException | JoranException e) {
			System.out.println("日志初始化失败!");
		}
		
		LOGGER.info("初始化服务器参数...");
		List<Map<String,String>> clientConfig = new ArrayList<Map<String,String>>();
		
		Map<String,String> config = new HashMap<String,String>();
		config.put("serverNode", "mc"); //服务节点
		config.put("remote", "localhost"); //服务节点IP地址
		config.put("port", "8090"); //端口号
		config.put("maxclients", "32"); //最大TCP连接数
		config.put("minclients", "6"); //最小TCP连接数
		config.put("speedNum", "1024"); //最大单条TCP速率[在途事务数]
		
		clientConfig.add(config);
		
		RpcParameter.INSTANCE.initRpcParameter(clientConfig);		
		RpcClientTaskPool.initPool();	
		RpcClientTimeOutScan.scan();
		try {
			RpcClient.start();
		} catch (Exception e1) {
			LOGGER.info("启动异常: " + e1.getMessage());			
		}
		LOGGER.info("初始化完毕...");
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		//NettyTestData data = new NettyTestData();
		
		HashMap<String,String> mapMessage = new HashMap<String,String>();
		mapMessage.put("certNo", "123456");
		
		@SuppressWarnings("rawtypes")
		RaptorRpc rpc = new RaptorRpc();
		
		//异步
		rpc.sendAsyncMessage("mc", "LoginAuth", new AbstractCallBack() {
			@Override
			public void invoke(RpcResponseBody resp) {
		        LOGGER.info("异步响应: " + resp);		        
			}
			
		}, 5, mapMessage, message);
		
		//同步
		LOGGER.enter("queryFinalResult","服务身份证信息查询[start]");
		long start = System.currentTimeMillis();
	    RpcResponseBody response = rpc.sendSyncMessage("mc", "LoginAuth", mapMessage, message);
		long end = System.currentTimeMillis();
		LOGGER.exit("queryFinalResult","服务身份证信息查询[end],result : " + response +", 耗时: " + (end- start));
	}

}