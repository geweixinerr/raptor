package raptor.core.message;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import raptor.core.AbstractCallBack;

/**
 * @author gewx RPC消息发送主体
 * **/
public final class RpcRequestBody implements RpcMessage {

	/**
	 * thread safe 
	 * **/
	private transient static final DateTimeFormatter DATE_FORMATE = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");
	
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
	 * 是否同步[true-同步,false-异步]
	 * **/
	private boolean isSync;
	
	/**
	 * 线程号-分布式日志采集
	 * **/
	private String traceId;
	
	/**
	 * 业务超时时间,默认5秒(单位:秒)
	 * **/
	private transient DateTime timeOut; 
	
	/**
	 * 业务-->请求时间
	 * **/
	private  transient DateTime requestTime; 
	
	/**
	 * 客户端调用回调对象
	 * **/
	private transient AbstractCallBack call;

	/**
	 * 发送标记. true已发送,false未发送
	 **/
	private transient boolean send;
	
	/**
	 * 延迟时间
	 * **/
	private transient Long delayTime;
	
	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public Long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Long delayTime) {
		this.delayTime = delayTime;
	}

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
	
	public DateTime getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(DateTime timeOut) {
		this.timeOut = timeOut;
	}

	public DateTime getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(DateTime requestTime) {
		this.requestTime = requestTime;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
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
		ToStringBuilder builder = new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE);
		builder.append("messageId",messageId);
		builder.append("rpcMethod",rpcMethod);
		builder.append("isSync",isSync);
		if (this.requestTime != null) {			
			builder.append("reqDate", this.requestTime.toString(DATE_FORMATE));
		}

		if (body != null) {
			builder.append("body",ArrayUtils.toStringArray(body));
		} else {
			builder.append("body","");
		}
		
		/*		
		 * 
		if (this.timeOut != null) {
			builder.append("timeOut",this.timeOut.toString(dateTimeFormat));
		}
		*/
		return builder.toString();
	}
	
}
