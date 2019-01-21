package raptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import raptor.RaptorRpc;
import raptor.core.AbstractCallBack;
import raptor.core.client.NettyTestData;
import raptor.core.client.RpcClient;
import raptor.core.client.RpcClientTaskPool;
import raptor.core.client.task.RpcClientTimeOutScan;
import raptor.core.init.RpcParameter;
import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;

/**
 * Raptor单元测试类
 * **/
public final class RaptorTest {
	
	static {
		System.out.println("初始化服务器参数...");
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
			System.out.println("启动异常: " + e1.getMessage());			
		}
		System.out.println("初始化完毕...");
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// 组装发送消息
		String message = "Netty RPC Send, Netty is VeryGood!";
		NettyTestData data = new NettyTestData();
		
		HashMap<String,String> mapMessage = new HashMap<String,String>();
		mapMessage.put("certNo", "123456");
		
		@SuppressWarnings("rawtypes")
		RaptorRpc rpc = new RaptorRpc();
		
		//异步
		/*
		rpc.sendAsyncMessage("mc", "LoginAuth", new AbstractCallBack() {
			@Override
			public void invoke(RpcResponseBody resp) {
				System.out.println("RPC结果[0]: " + resp);
			}
			
			@Override
			public void invoke(RpcRequestBody req, RpcResponseBody resp) {
				System.out.println("请求对象: " + req);
				System.out.println("RPC结果[1]: " + resp);
			}
			
		}, 5, data, message);
		*/
		
		//同步
		long start = System.currentTimeMillis();
	    RpcResponseBody response = rpc.sendSyncMessage("mc", "LoginAuth", mapMessage, message);
		long end = System.currentTimeMillis();
		
		System.out.println("result : " + response +", 耗时: " + (end- start));
	}

}