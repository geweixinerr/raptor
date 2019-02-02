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
import raptor.core.RpcResult;
import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.client.task.RpcClientTimeOutScan;
import raptor.core.init.RpcParameter;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
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
		final String methodName = "testMethod";
		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		NettyTestData data = new NettyTestData();
		
		HashMap<String,String> mapMessage = new HashMap<String,String>();
		mapMessage.put("certNo", "123456");
		
		@SuppressWarnings("rawtypes")
		RaptorRpc rpc = new RaptorRpc();
		
		System.out.println("RPC调用开始===================================>");
		//异步
		try {
			rpc.sendAsyncMessage("mc", "LoginAuth", new AbstractCallBack() {
				@Override
				public void invoke(RpcResponseBody response) {
					if (response.getRpcCode().equals(RpcResult.SUCCESS)) {
						LOGGER.info(methodName, "服务调用SUCCESS~ " + response);
					} else if (response.getRpcCode().equals(RpcResult.FAIL)) {
						LOGGER.warn(methodName, "服务端业务执行异常~");
					} else if (response.getRpcCode().equals(RpcResult.TIME_OUT) || response.getRpcCode().equals(RpcResult.SCAN_TIME_OUT)) {
						LOGGER.warn(methodName, "RPC调用超时~");
					} else if (RpcResult.FAIL_NETWORK_TRANSPORT.equals(response.getRpcCode())) {
						LOGGER.error(methodName, "数据传输异常, rpcCode: " + response.getRpcCode());
					} else {
						LOGGER.warn(methodName, "服务调用异常, rpcCode: " + response.getRpcCode());
					}
				}
			}, 5, data, message);
		} catch (RpcException e) {
			if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
				LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
			} else {
				LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
			}
		}
		
		//同步
		LOGGER.enter(methodName, "服务身份证信息查询[start]");
		try {
			RpcResponseBody response = rpc.sendSyncMessage("mc", "LoginAuth", mapMessage, message);
			if (response.getRpcCode().equals(RpcResult.SUCCESS)) {
				LOGGER.info(methodName, "服务调用SUCCESS~");
			} else if (response.getRpcCode().equals(RpcResult.FAIL)) {
				LOGGER.warn(methodName, "服务端业务执行异常~");
			} else if (response.getRpcCode().equals(RpcResult.TIME_OUT)) {
				LOGGER.warn(methodName, "RPC调用超时~");
			} else if (RpcResult.FAIL_NETWORK_TRANSPORT.equals(response.getRpcCode())) {
				LOGGER.error(methodName, "数据传输异常");
			} else {
				LOGGER.warn(methodName, "服务调用异常");
			}
		} catch (RpcException e) {
			if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
				LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
			} else {
				LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
			}
		}
		LOGGER.exit(methodName, "服务身份证信息查询[end]");
	}

}