package raptor.core.message;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.Period;

import raptor.core.AbstractCallBack;

/**
 * @author gewx RPC消息发送主体
 * **/
public final class RpcRequestBody implements RpcMessage, Delayed {

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
	 * 是否同步[true-同步,false-异步]
	 * **/
	private boolean isSync;
	
	/**
	 * 线程号-分布式日志采集
	 * **/
	private String threadId;
	
	/**
	 * 业务超时时间,默认5秒(单位:秒)
	 * **/
	private transient DateTime timeOut; 
	
	/**
	 * 业务-->请求时间
	 * **/
	private  transient DateTime requestTime; 
	
	/**
	 * 业务-->客户端回调时间
	 * **/
	private  transient DateTime responseTime; 
	
	/**
	 * 业务-->服务器响应达到客户端时间
	 * **/
	private  transient DateTime clientTime; 
	
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
	
	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
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

	public DateTime getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(DateTime responseTime) {
		this.responseTime = responseTime;
	}

	public DateTime getClientTime() {
		return clientTime;
	}

	public void setClientTime(DateTime clientTime) {
		this.clientTime = clientTime;
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
	public int compareTo(Delayed o) {
		if (this == o) {
			return 0;
		}
		
		RpcRequestBody other = (RpcRequestBody)o;
		if (this.getDelayTime() > other.getDelayTime()) {
			return 1;
		} else if (this.getDelayTime() < other.getDelayTime()) {
			return -1;
		} else {
			return 0;	
		}
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(this.getDelayTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
		
		/*		
		 * DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");
		if (this.requestTime != null) {
			builder.append("reqDate", this.requestTime.toString(dateTimeFormat));
		}
		
		if (this.timeOut != null) {
			builder.append("timeOut",this.timeOut.toString(dateTimeFormat));
		}
		
		if (this.clientTime != null) {
			builder.append("respDate", this.clientTime.toString(dateTimeFormat));
		}
		
		if (this.responseTime != null) {
			builder.append("callDate", this.responseTime.toString(dateTimeFormat));
		}
		
		builder.append("isSync",isSync);
		*/
		
		if(this.requestTime != null && this.responseTime != null) {
			Period p2 = new Period(this.requestTime, this.responseTime);
			int seconds = p2.getSeconds(); //相差的秒
			builder.append("RPC耗时: " + (seconds * 1000 + p2.getMillis()));
		}
		
		sb.trimToSize();
		return builder.toString();
	}
	
}
