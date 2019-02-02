package raptor.core.message;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

import raptor.core.RpcResult;

/**
 * @author gewx RPC消息响应主体
 * **/
public final class RpcResponseBody implements RpcMessage {

	/**
	 */
	private static final long serialVersionUID = 495386357818590739L;
    
	/**
	 * 消息Id
	 * **/
	private String messageId; 
	
	/**
	 * 消息描述
	 **/
	private String message;
	
	/**
	 * 消息主体-响应结果
	 * **/
	private Object body;
	
	/**
	 * 服务器执行方法名
	 * **/
	private String rpcMethod;

	/**
	 * RPC响应码
	 * **/
	private RpcResult rpcCode;
	
	/**
	 * 线程号-分布式日志采集
	 * **/
	private String threadId;
	
	/**
	 * 业务-->服务器响应到达客户端时间
	 * **/
	private  transient DateTime returnTime; 
	
	/**
	 * 业务-->RPC耗时
	 * **/
	private  transient Integer rpcTime; 

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public RpcResult getRpcCode() {
		return rpcCode;
	}

	public void setRpcCode(RpcResult rpcCode) {
		this.rpcCode = rpcCode;
	}

	public String getRpcMethod() {
		return rpcMethod;
	}

	public void setRpcMethod(String rpcMethod) {
		this.rpcMethod = rpcMethod;
	}

	public DateTime getReturnTime() {
		return returnTime;
	}

	public void setReturnTime(DateTime returnTime) {
		this.returnTime = returnTime;
	}

	public Integer getRpcTime() {
		return rpcTime;
	}

	public void setRpcTime(Integer rpcTime) {
		this.rpcTime = rpcTime;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE);
		builder.append("message",message);
		builder.append("rpcCode",rpcCode.getCode());
		
		/**
		 * DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS");
		 * builder.append("returnTime",returnTime);
		 * **/
		builder.append("rpcTime",rpcTime);
		builder.append("body",body);
		return builder.toString();
	}
	
}
