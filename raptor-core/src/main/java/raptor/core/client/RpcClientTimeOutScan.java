package raptor.core.client;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;
import raptor.core.server.RpcResult;

/**
 * @author gewx RPC请求客户端超时扫描: 本线程执行过期消息清理.
 * **/
public final class RpcClientTimeOutScan {

	/**
	 * 启动单例线程扫描
	 * **/
	private static final Executor execute = Executors.newSingleThreadExecutor();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientTimeOutScan.class);

	private RpcClientTimeOutScan() {
	}

	/**
	 * 扫描超时请求.
	 * **/
	public static void scan() {
		LOGGER.info("客户端超时消息清理器启动...");
		execute.execute(new Runnable(){
			@Override
			public void run() {
				while (true) {
					Map<String,RpcRequestBody> requestPool = RpcClientTaskPool.getClientTaskPool();
					for (Map.Entry<String, RpcRequestBody> en : requestPool.entrySet()) {						
						String messageId = en.getKey();
						RpcRequestBody requestBody = en.getValue();
						//如果当前时间已超过设置的超时时间,则为过期消息.
						if (new Date().compareTo(requestBody.getTimeOut()) >= 0) {
							requestPool.remove(messageId); //delete
							
							final RpcResult result = new RpcResult();
							//default							
							RpcResponseBody responseBody = new RpcResponseBody(); 
							responseBody.setSuccess(false);
							responseBody.setMessageId(requestBody.getMessageId());
							responseBody.setMessage("RPC 服务调用失败,message:timeOut.");
							
							result.setSuccess(false);
							result.setMessageBody(responseBody);
							
							requestBody.getCall().invoke(result); //回调通知
							LOGGER.info("清理超时消息...,messageId: " + messageId);
						}
					}
				}
			}
		});
	}
}
