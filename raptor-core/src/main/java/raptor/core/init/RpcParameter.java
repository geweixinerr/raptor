package raptor.core.init;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * RPC参数存储单例类
 * 
 * @author gewx
 * **/
public final class RpcParameter {

	private List<Map<String,String>> clientConfig; 
	
	private Map<String,String> serverConfig; 
	
	private RpcParameter() {}
	
	public static final RpcParameter INSTANCE = new RpcParameter();
	
	public void initRpcParameter(List<Map<String,String>> client , Map<String,String> server) {			
		clientConfig = Collections.unmodifiableList(client);
        serverConfig = Collections.unmodifiableMap(server);
	}
	
	public void initRpcParameter(List<Map<String,String>> client) {			
		clientConfig = Collections.unmodifiableList(client);
	}
	
	public void initRpcParameter(Map<String,String> server) {			
        serverConfig = Collections.unmodifiableMap(server);
	}
	
	public List<Map<String,String>> getClientConfig() {
		return clientConfig;
	}
	
	public Map<String,String> getServerConfig() {
		return serverConfig;
	}
	
}
