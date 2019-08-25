package raptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.util.ResourceUtils;

import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.ext.spring.LogbackConfigurer;
import raptor.core.AbstractCallBack;
import raptor.core.RpcResult;
import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClientManualBuilder;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;
import raptor.util.StringUtil;

/**
 * Raptor客户端测试类
 **/
public final class RaptorClientTest {

	private static final RaptorLogger LOGGER = new RaptorLogger(RaptorClientTest.class);

	static {
		try {
			File file = ResourceUtils.getFile("classpath:raptorLogback.xml");
			LogbackConfigurer.initLogging(file.toURI().toString());
		} catch (FileNotFoundException | JoranException e) {
			System.out.println("日志初始化失败!");
		}

		RpcClientManualBuilder clientBuilder = new RpcClientManualBuilder.Builder().serverNode("mc").remote("localhost")
				.port(8090).addClient().build();

		try {
			clientBuilder.connection();
		} catch (Exception e) {
			LOGGER.error("客户端初始化异常: " + StringUtil.getErrorText(e));
		}
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		final String methodName = "testMethod";
		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		NettyTestData data = new NettyTestData();

		HashMap<String, String> mapMessage = new HashMap<String, String>();
		mapMessage.put("certNo", "123456");

		@SuppressWarnings("rawtypes")
		RaptorRpc rpc = new RaptorRpc();

		LOGGER.info("RPC调用开始===================================>", false);

		// 异步
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

		// 同步
		/*
		 * LOGGER.info(methodName, "服务身份证信息查询[start]"); try { RpcResponseBody response =
		 * rpc.sendSyncMessage("mc", "LoginAuth", mapMessage, message);
		 * LOGGER.info("RPC同步响应: " + response); if
		 * (RpcResult.SUCCESS.equals(response.getRpcCode())) { LOGGER.info(methodName,
		 * "服务调用SUCCESS~"); } else { LOGGER.warn(methodName, "RPC服务调用异常!"); } } catch
		 * (RpcException e) { if
		 * (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
		 * LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage()); } else {
		 * LOGGER.error(methodName, "其它异常, message: " + e.getMessage()); } }
		 * LOGGER.exit(methodName, "服务身份证信息查询[end]");
		 */

		boolean isTest = false;
		if (isTest) {
			// 测试分布式日志
			int cpuNum = Runtime.getRuntime().availableProcessors();
			Executor pool = Executors.newFixedThreadPool(cpuNum);
			CyclicBarrier lock = new CyclicBarrier(cpuNum);
			for (int i = 0; i < cpuNum * 100; i++) {
				pool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							lock.await();
						} catch (Exception e1) {
						}
						final String methodName = "threadRun";
						// 参数false作用为:不复用traceId,每次请求都作为单独的事务开始.
						LOGGER.enter(methodName, "RPC execute start!", false);
						try {
							RpcResponseBody resp = rpc.sendSyncMessage("mc", "LoginAuth");
							LOGGER.info("RPC同步响应: " + resp);
							if (RpcResult.SUCCESS.equals(resp.getRpcCode())) {
								LOGGER.info(methodName, "同步RPC服务调用SUCCESS~");
							} else {
								LOGGER.warn(methodName, "同步RPC服务调用异常!");
							}
						} catch (RpcException e) {
							if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
								LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
							} else {
								LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
							}
						}

						LOGGER.info("RPC异步请求开始...");
						try {
							rpc.sendAsyncMessage("mc", "LoginAuth", new AbstractCallBack() {
								@Override
								public void invoke(RpcResponseBody resp) {
									LOGGER.info("RPC异步响应: " + resp);
									if (RpcResult.SUCCESS.equals(resp.getRpcCode())) {
										LOGGER.info(methodName, "异步RPC服务调用SUCCESS~");
									} else {
										LOGGER.warn(methodName, "异步RPC服务调用异常!");
									}
								}
							});
						} catch (RpcException e) {
							if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
								LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
							} else {
								LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
							}
						}

						LOGGER.enter(methodName, "RPC execute end!");
					}
				});
			}
		}
	}

}