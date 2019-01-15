package raptor.core.init;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import raptor.core.annotation.RpcMethod;

/**
 * @author gewx RPC服务端映射初始化类
 * **/
public final class RpcMappingInit {

	private RpcMappingInit() {
	}
	
	private static final Map<String,RpcHandlerObject> RPC_MAPPING = new ConcurrentHashMap<String,RpcHandlerObject>(64);
			
	/**
	 * @author gewx 建立RPC映射关系
	 * **/
	public static void initRpcMapping(Map<String,Object> mappingMap) {
		for (Map.Entry<String, Object> en : mappingMap.entrySet()) {
			Object rpcObject = en.getValue();
			Method [] method = en.getValue().getClass().getMethods();
            for (Method m : method) {
            	if (m.isAnnotationPresent(RpcMethod.class)) {
            		RpcHandlerObject handler = new RpcHandlerObject();
            		handler.setObject(rpcObject);
            		handler.setRpcKey(m.getName());
            		RPC_MAPPING.put(m.getName(), handler); //便于未来拓展,这里设置为一个POJO映射实体.
            	}
            }
		}
	} 

	public static Map<String,RpcHandlerObject> listRpcMapping() {
		return Collections.unmodifiableMap(RPC_MAPPING);
	}
	
}
