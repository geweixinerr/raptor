package raptor.service;

import java.util.Map;

import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;
import raptor.core.client.NettyTestData;

/**
 * @author gewx 业务服务类 Test类
 * **/

@RpcHandler
public final class AlibabaService {

	/**登录认证**/
	@RpcMethod
	public void LoginAuth(Map<String,String> params) {
		System.out.println("AlibabaService.............");
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(Map<String,String> params,String authId) {
		System.out.println("AlibabaService.............");
		return "Netty is VeryGood!";
	}
	
	/**登录认证**/
	@RpcMethod
	public String LoginAuth(NettyTestData params,String authId) {
		System.out.println("AlibabaService............." + params);
		return "Netty is VeryGood!";
	}
	
}
