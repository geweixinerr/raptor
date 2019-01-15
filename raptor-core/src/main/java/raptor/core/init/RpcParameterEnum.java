package raptor.core.init;

/**
 * @author gewx RPC配置参数枚举
 * **/
public enum RpcParameterEnum {

	LOGONOFF("logOnOff","Netty默认日志开关.");
	
	RpcParameterEnum(String code,String comment) {
		this.code = code;
		this.comment = comment;
	}
	
	private String code; 
	
	private String comment; 

	public String getCode() {
		return code;
	}

	public String getComment() {
		return comment;
	} 

}
