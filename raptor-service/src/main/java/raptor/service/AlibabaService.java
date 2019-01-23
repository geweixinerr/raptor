package raptor.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;
import raptor.core.client.NettyTestData;
import raptor.dao.member.MemberInfoDao;
import raptor.log.RaptorLogger;

/**
 * @author gewx 业务服务类 Test类
 * **/

@RpcHandler
public final class AlibabaService {

	private static final RaptorLogger LOGGER = new RaptorLogger(AlibabaService.class);
	
	@Autowired
	private MemberInfoDao memberInfoDao;
	
	/**登录认证**/
	@RpcMethod
	public void LoginAuth(Map<String,String> params) {
		
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(Map<String,String> params,String message) {
		String methodName = "LoginAuth";
		LOGGER.enter(methodName, "单点服务认证请求,params: " + params +",message : " + message);
		String msg = "SUCCESS";
		LOGGER.exit(methodName, "单点认证服务请求结束!");
		return msg;
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(NettyTestData params,String message) {
		String dbMessage = memberInfoDao.selectOne();
		return message +", dbMessage: " + dbMessage;
	}
	
}
