package raptor.core.init;

import java.util.Collections;
import java.util.Map;

/**
 * @author gewx RPC参数存储单例类
 * **/
public final class RpcParameter {

	private Map<String,String> clientConfig; //客户端参数配置
	
	private Map<String,String> serverConfig; //服务端参数配置
	
	private RpcParameter() {}
	
	public static final RpcParameter INSTANCE = new RpcParameter();
	
	/**
	 * 初始化存储客户端/服务端参数
	 * **/
	public void initRpcParameter(Map<String,String> client , Map<String,String> server) {
		clientConfig = Collections.unmodifiableMap(client);
        serverConfig = Collections.unmodifiableMap(server);
	}
	
	/**
	 * 获取client参数配置
	 * **/
	public Map<String,String> getClientConfig() {
		return clientConfig;
	}
	
	/**
	 * 获取server参数配置
	 * **/
	public Map<String,String> getServerConfig() {
		return serverConfig;
	}
	
}