package raptor.core;

/**
 * @author gewx RPC请求响应码枚举
 **/
public enum RpcResult {

	FLOWER_CONTROL("流控"),
	SUCCESS("成功"),
	FAIL_NETWORK_CONNECTION("网络初始化连接失败"),
	FAIL_NETWORK_TRANSPORT("网络传输失败"),
	ERROR("其它异常"), 
	TIME_OUT("超时"), 
	SCAN_TIME_OUT("扫描超时");

	RpcResult(String comment) {
		this.comment = comment;
	}

	private String comment;

	public String getComment() {
		return comment;
	}

}
