package raptor.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;
import raptor.core.client.NettyTestData;
import raptor.dao.member.MemberInfoDao;

/**
 * @author gewx 业务服务类 Test类
 * **/

@RpcHandler
public final class AlibabaService {

	@Autowired
	private MemberInfoDao memberInfoDao;
	
	/**登录认证**/
	@RpcMethod
	public void LoginAuth(Map<String,String> params) {
		
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(Map<String,String> params,String message) {
		return message;
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(NettyTestData params,String message) {
		String dbMessage = memberInfoDao.selectOne();
		return message +", dbMessage: " + dbMessage;
	}
	
}
