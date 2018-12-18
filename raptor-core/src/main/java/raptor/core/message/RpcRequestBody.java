package raptor.core.message;

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import raptor.core.AbstractCallBack;

/**
 * @author gewx RPC消息发送主体
 * **/
public final class RpcRequestBody implements RpcMessage {

	/**
	 */
	private static final long serialVersionUID = 1584389395921234145L;

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
	private transient Date timeOut; 
	
	/**
	 * 客户端调用回调对象
	 * **/
	private transient AbstractCallBack call;

	/**
	 * 发送标记. true已发送,false未发送
	 **/
	private transient boolean send;
	
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

	public Date getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(Date timeOut) {
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
	
	/**
	 * @author gewx 本方法作用在于串行化回调消息执行逻辑. 问题描述:
	 *         假设调度线程与正常客户端回调线程同时处理某个回调对象,会产生客户端响应接收到两次的情况.
	 **/
	public synchronized boolean isMessageSend() {
		if (this.send) { // 已发送
			return true;
		} else { // 未发送
			this.send = true;
			return false;
		}
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
		sb.trimToSize();
		return builder.toString();
	}
	
}
