package raptor.core;

import org.joda.time.DateTime;

import raptor.core.message.RpcRequestBody;
import raptor.exception.RpcException;

/**
 * @author gewx RPC服务接口定义. Channel与业务服务方法解耦层.
 * **/
public interface RpcPushDefine {

	/**
	 * @author gewx 消息推送.
	 * @param RpcRequestBody RPC请求对象, call 资源释放回调对象.
	 * @throws RpcException RPC异常
	 * @return void 
	 * **/
	
	void pushMessage(RpcRequestBody requestBody) throws RpcException;
	
	/**
	 * @author gewx 资源关闭,释放tcp pool空闲对象.
	 * **/
	void close();
	
	/**
	 * @author gewx 检测tcp pool当中tcp对象,是否可写.
	 * */
	boolean isWritable();
	
	/**
	 * @author gewx tcp连接唯一Id [建议测试使用].
	 * **/
	String getTcpId();
	
	/**
	 * @author gewx 获取tcp连接入池时间 [建议测试使用].
	 * **/
	DateTime getTcpIntoPoolTime();
	
	/**
	 * @author gewx 心跳检测包方法名
	 * **/
	String HEARTBEAT_METHOD = "heartbeat";
}
