package raptor.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import raptor.core.RpcResult;
import raptor.core.annotation.RpcHandler;
import raptor.core.annotation.RpcMethod;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;
import raptor.plugin.demo.DemoPlugin;

/**
 * gewx 业务服务类 Test类
 * 
 * @author 
 * **/

@RpcHandler
@Service
public final class AlibabaService {

	private static final RaptorLogger LOGGER = new RaptorLogger(AlibabaService.class);
	
	@Autowired
	private DemoPlugin plugin;

	/**
	 * @author gewx 剔除MySQL操作
	 * **/
	/*
	@Autowired
	private MemberInfoDao memberInfoDao;
	*/
	/**登录认证**/
	@RpcMethod
	public String loginAuth() {
		LOGGER.info("loginAuth----------------->");
		return "Netty is VeryGood!";
	}
	
	/**登录认证**/
	
	@RpcMethod
	public String loginAuth(Map<String,String> params,String message) {
		String methodName = "loginAuth";
		LOGGER.enter(methodName, "单点服务认证请求,params: " + params +",message : " + message);
		StringBuilder sb = new StringBuilder();
		try {
			Object result = plugin.casLonginAuth();
			if (result != null) {
				sb.append(result);	
			}
		} catch (RpcException e) {
			//根据实际业务,处理.这里只打印日志.
			if (RpcResult.FAIL_NETWORK_CONNECTION.equals(e.getRpcCode())) {
				LOGGER.error(methodName, "网络连接异常, message: " + e.getMessage());
			} else {
				LOGGER.error(methodName, "其它异常, message: " + e.getMessage());
			}
		}
		LOGGER.exit(methodName, "单点认证服务请求结束!");
		return sb.toString();
	}
}
