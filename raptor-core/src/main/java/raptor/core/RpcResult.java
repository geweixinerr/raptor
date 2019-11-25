package raptor.core;

/**
 * RPC请求响应码枚举
 * 
 * @author gewx
 **/
public enum RpcResult {

	// RPC调用成功
	SUCCESS("SUCCESS", "RPC调用成功"),

	// 服务端异常,RPC调用失败
	FAIL("FAIL", "服务端异常,RPC调用失败"),

	// 其它异常
	ERROR("ERROR", "其它异常"),

	// 流控
	FLOWER_CONTROL("FLOWER_CONTROL", "流控"),

	// 网络初始化连接失败
	FAIL_NETWORK_CONNECTION("FAIL_NETWORK_CONNECTION", "网络初始化连接失败"),

	// 网络传输失败
	FAIL_NETWORK_TRANSPORT("FAIL_NETWORK_TRANSPORT", "网络传输失败"),

	// 超时
	TIME_OUT("TIME_OUT", "超时"),

	// 扫描超时
	SCAN_TIME_OUT("SCAN_TIME_OUT", "扫描超时");

	RpcResult(String code, String comment) {
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
