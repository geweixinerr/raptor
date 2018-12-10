package raptor.core.server;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author gewx RPC调用回调响应结果
 **/
public final class RpcResult {

	/**
	 * 业务执行是否成功标记, true/false
	 **/
	private Boolean success;

	/**
	 * 消息描述
	 **/
	private String message;

	/**
	 * 业务执行异常对象(如果有的话)
	 **/
	private Throwable throwable;

	public RpcResult() {
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(128); // init
		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE, sb);
		builder.append("success", success).append("message", message);
		if (this.throwable != null) {
			builder.append("throwable", this.throwable.getMessage());
		} else {
			builder.append("throwable", "");
		}

		sb.trimToSize(); // clean

		return builder.toString();
	}
}
