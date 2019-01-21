package raptor.core;

/**
 * @author gewx 常量类
 * **/
public final class Constants {

	private Constants() {
	}
	
	/**
	 * Netty日志开启开关-开
	 * **/
	public static final String LogOn = "On";
	
	/**
	 * Netty日志开启开关-关
	 * **/
	public static final String LogOff = "Off";
	
	/**
	 * ChannelHandler默认注册名
	 * **/
	public static final String CLIENT_DISPATCHER = "clientDispatcher";
	
	/**
	 * CPU核心数
	 * **/
	public static final Integer CPU_CORE = Runtime.getRuntime().availableProcessors();
	
	/**
	 * 1KB数值常量.
	 * **/
	public static final Integer ONE_KB = 1024; 
	
	/**
	 * logback MDC
	 * **/
	public static final String MDC_KEY = "invokeId";
}
