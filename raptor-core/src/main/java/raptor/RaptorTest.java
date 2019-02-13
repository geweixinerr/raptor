package raptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.util.ResourceUtils;

import com.eaio.uuid.UUID;

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
import raptor.log.ThreadContext;

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
		
		LOGGER.info("RPC调用开始===================================>");

		//异步
		try {
			rpc.sendAsyncMessage("mc", "LoginAuth", new AbstractCallBack() {
				@Override
				public void invoke(RpcResponseBody response) {
					LOGGER.info("RPC异步响应: " + response);
					if (response.getRpcCode().equals(RpcResult.SUCCESS)) {
						LOGGER.info(methodName, "服务调用SUCCESS~ ");
					} else {
						LOGGER.warn(methodName, "RPC服务调用异常!");
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
			} else {
				LOGGER.warn(methodName, "RPC服务调用异常!");
			}
		} catch (RpcException e) {
			if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
				LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
			} else {
				LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
			}
		}
		LOGGER.exit(methodName, "服务身份证信息查询[end]");
		
		boolean isTest = false;
		if (isTest) {
			//测试分布式日志
			int cpuNum = Runtime.getRuntime().availableProcessors();
			Executor pool = Executors.newFixedThreadPool(cpuNum);
			CyclicBarrier lock = new CyclicBarrier(cpuNum);
			for (int i = 0; i < cpuNum * 10; i++) {
				pool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							lock.await();
						} catch (Exception e1) {
						}
						ThreadContext.TRACEID.set(new UUID().toString());
						try {
							RpcResponseBody response = rpc.sendSyncMessage("mc", "LoginAuth");
							if (response.getRpcCode().equals(RpcResult.SUCCESS)) {
								LOGGER.info(methodName, "服务调用SUCCESS~");
							} else {
								LOGGER.warn(methodName, "RPC服务调用异常!");
							}
						} catch (RpcException e) {
							if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
								LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
							} else {
								LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
							}
						}
					}
				});
			}
		}
	}

}