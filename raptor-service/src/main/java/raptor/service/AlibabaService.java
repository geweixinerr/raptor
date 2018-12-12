package raptor.service;

import java.util.HashMap;
import java.util.Map;

import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;

/**
 * @author gewx 业务服务类 Test类
 * **/

@RpcHandler
public final class AlibabaService {

	/**登录认证**/
	@RpcMethod
	public void LoginAuth(Map<String,String> params) {
		System.out.println("AlibabaService............." + params);
	}
	
	/**登录认证**/
	@RpcMethod
	public void LoginAuth(Map<String,String> params,String authId) {
		System.out.println("AlibabaService............." + params +",authId: " +authId);
	}
	
}
