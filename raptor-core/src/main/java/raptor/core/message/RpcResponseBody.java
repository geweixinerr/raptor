package raptor.core.message;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
	 * 执行标记
	 * **/
	private Boolean success;

	/**
	 * RPC响应码
	 * **/
	private transient RpcResult rpcCode;
	
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

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public RpcResult getRpcCode() {
		return rpcCode;
	}

	public void setRpcCode(RpcResult rpcCode) {
		this.rpcCode = rpcCode;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(256);
		ToStringBuilder builder = new ToStringBuilder(this,ToStringStyle.SHORT_PREFIX_STYLE,sb);
		builder.append("message",message);
		builder.append("messageId",messageId);
		builder.append("success",success);
		builder.append("body",body);
		if (this.rpcCode != null) {
			builder.append("rpcCode",rpcCode.getComment());	
		}
		sb.trimToSize();
		return builder.toString();
	}
	
}
