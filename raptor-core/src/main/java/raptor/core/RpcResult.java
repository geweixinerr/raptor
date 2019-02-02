package raptor.core;

/**
 * @author gewx RPC请求响应码枚举
 **/
public enum RpcResult {

	FLOWER_CONTROL("FLOWER_CONTROL", "流控"),
	SUCCESS("SUCCESS", "RPC调用成功"),
	FAIL("FAIL", "服务端异常,RPC调用失败"),
	FAIL_NETWORK_CONNECTION("FAIL_NETWORK_CONNECTION", "网络初始化连接失败"),
	FAIL_NETWORK_TRANSPORT("FAIL_NETWORK_TRANSPORT", "网络传输失败"),
	ERROR("ERROR", "其它异常"), 
	TIME_OUT("TIME_OUT", "超时"), 
	SCAN_TIME_OUT("SCAN_TIME_OUT", "扫描超时");

	RpcResult(String code , String comment) {
		this.code = code;
		this.comment = comment;
	}
	
	private String code;

	private String comment;

	
	public String getComment() {
		return comment;
	}

	public String getCode() {
		return code;
	}

}
