package raptor.core.init;

/**
 * 映射对象实体
 * 
 * @author gewx 
 * **/
public final class RpcHandlerObject {

	public RpcHandlerObject() {}
	
	/**
	 * RPC调用反射映射Key,一般为服务端方法名.
	 * **/
	private String rpcKey;
	
	/**
	 * RPC服务调用主体.
	 * **/
	private Object object;

	public String getRpcKey() {
		return rpcKey;
	}

	public void setRpcKey(String rpcKey) {
		this.rpcKey = rpcKey;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
