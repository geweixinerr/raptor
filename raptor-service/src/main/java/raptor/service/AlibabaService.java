package raptor.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import raptor.RaptorRpc;
import raptor.core.RpcResult;
import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;
import raptor.core.client.NettyTestData;
import raptor.core.message.RpcResponseBody;
import raptor.dao.member.MemberInfoDao;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;

/**
 * @author gewx 业务服务类 Test类
 * **/

@RpcHandler
public final class AlibabaService {

	private static final RaptorLogger LOGGER = new RaptorLogger(AlibabaService.class);
	
	private static final String serverNode = "tcs";
	
	@SuppressWarnings("rawtypes")
	private static final RaptorRpc rpc = new RaptorRpc();
	
	@Autowired
	private MemberInfoDao memberInfoDao;
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth() {
		LOGGER.info("LoginAuth----------------->");
		return "Netty is VeryGood!";
	}
	
	/**登录认证**/
	
	@SuppressWarnings("unchecked")
	@RpcMethod
	public String LoginAuth(Map<String,String> params,String message) {
		String methodName = "LoginAuth";
		LOGGER.enter(methodName, "单点服务认证请求,params: " + params +",message : " + message);
		StringBuilder sb = new StringBuilder();
		try {
			RpcResponseBody response = rpc.sendSyncMessage(serverNode, "LoginAuth");
			if (response.getRpcCode().equals(RpcResult.SUCCESS)) {
				LOGGER.info(methodName, "服务调用SUCCESS~");
				sb.append(response.getBody());
			} else {
				LOGGER.warn(methodName, "服务调用失败, message: " + response);
				sb.append("Fail");
			}
		} catch (RpcException e) {
			if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
				LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
			} else {
				LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
			}
			sb.append("Error");
		}
		
		LOGGER.exit(methodName, "单点认证服务请求结束!");
		return sb.toString();
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(NettyTestData params,String message) {
		String dbMessage = memberInfoDao.selectOne();
		return message +", dbMessage: " + dbMessage;
	}
	
}
