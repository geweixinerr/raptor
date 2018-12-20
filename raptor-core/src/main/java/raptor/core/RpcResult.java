package raptor.core;

/**
 * @author gewx RPC请求响应码枚举
 * **/
public enum RpcResult {
	
	FLOWER_CONTROL("流控"),SUCCESS("成功"),FAIL("失败"),TIME_OUT("超时");
	
	RpcResult(String comment) {
		this.comment = comment;
	}
	
	private String comment; //描述文本


	public String getComment() {
		return comment;
	}

}
