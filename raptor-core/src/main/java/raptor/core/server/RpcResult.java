package raptor.core.server;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import raptor.core.message.RpcResponseBody;

/**
 * @author gewx RPC调用回调响应结果
 **/
public final class RpcResult {

	/**
	 * 业务执行是否成功标记, true/false
	 **/
	private Boolean success;
	
	/**
	 * 响应主体
	 * **/
	private RpcResponseBody responseBody;
	
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

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public RpcResponseBody getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(RpcResponseBody responseBody) {
		this.responseBody = responseBody;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(512); // init
		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE, sb);
		builder.append("success", success);
		builder.append("responseBody", responseBody);
		if (this.throwable != null) {
			builder.append("throwable", this.throwable.getMessage());
		} else {
			builder.append("throwable", "");
		}

		sb.trimToSize(); // clean

		return builder.toString();
	}
}
