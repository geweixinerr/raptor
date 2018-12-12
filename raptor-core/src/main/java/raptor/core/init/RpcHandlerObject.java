package raptor.core.init;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author gewx 映射对象实体
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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(256);
		ToStringBuilder builder = new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE,sb);
		builder.append("rpcKey",rpcKey);
		builder.append("object",object);
		sb.trimToSize();
		return builder.toString();
	}

}
