package raptor.core;

/**
 * @author gewx 消息推送回调,主要用于释放tcp连接池资源.
 * **/
public abstract class PushMessageCallBack {
	
	private final RpcPushDefine rpcObject; 

	public PushMessageCallBack(RpcPushDefine rpcObject) {
		this.rpcObject = rpcObject;
	}
	
	/**
	 * @author gewx 回调方法,本方法主要用于tcp资源回收.
	 * @param rpcObject tcp资源池对象.
	 * **/
	public abstract void invoke();

	/**
	 * @author gewx 返回rpc Object对象
	 * **/
	public RpcPushDefine getRpcObject() {
		return this.rpcObject;
	}
}
