package raptor.core.message;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import raptor.core.AbstractCallBack;

/**
 * @author gewx RPC消息发送主体
 * **/
public final class RpcRequestBody implements RpcMessage , Serializable {

	/**
	 * serializable Id
	 */
	private static final long serialVersionUID = -1741781103121149152L;

	/**
	 * 消息Id
	 * **/
	private String messageId; 
	
	/**
	 * Rpc MethodName
	 * **/
	private String rpcMethod;
	
	/**
	 * 消息主体-请求参数
	 * **/
	private Object[] body;
	
	/**
	 * 业务超时时间,默认5秒(单位:秒)
	 * **/
	private transient Integer timeOut; 
	
	/**
	 * 客户端调用回调对象
	 * **/
	private transient AbstractCallBack call;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public Object[] getBody() {
		return body;
	}

	public void setBody(Object[] body) {
		this.body = body;
	}

	public Integer getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Integer timeOut) {
		this.timeOut = timeOut;
	}

	public AbstractCallBack getCall() {
		return call;
	}

	public void setCall(AbstractCallBack call) {
		this.call = call;
	}

	public String getRpcMethod() {
		return rpcMethod;
	}

	public void setRpcMethod(String rpcMethod) {
		this.rpcMethod = rpcMethod;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(256);
		ToStringBuilder builder = new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE,sb);
		builder.append("messageId",messageId);
		builder.append("rpcMethod",rpcMethod);
		if (body != null) {
			builder.append("body",ArrayUtils.toStringArray(body));
		} else {
			builder.append("body","");
		}
		builder.append("timeOut",timeOut);
		sb.trimToSize();
		return builder.toString();
	}
	
}
