package raptor.core;

/**
 * 常量类
 * 
 * @author gewx 
 * **/
public final class Constants {

	private Constants() {
	}

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
	
}
