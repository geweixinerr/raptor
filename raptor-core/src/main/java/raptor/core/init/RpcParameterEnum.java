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
	
	private String code; //RPC参数数值
	
	private String comment; //RPC参数描述

	public String getCode() {
		return code;
	}

	public String getComment() {
		return comment;
	} 

}
