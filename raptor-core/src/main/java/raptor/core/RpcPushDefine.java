package raptor.core;

import org.apache.commons.pool2.ObjectPool;

import raptor.core.message.RpcRequestBody;
import raptor.exception.RpcException;

/**
 * @author gewx RPC服务接口定义. Channel与业务服务方法解耦层.
 * **/
public interface RpcPushDefine {

	/**
	 * @author gewx 消息推送.
	 * @param RpcRequestBody RPC请求对象
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
	 * @author gewx tcp连接唯一Id
	 * **/
	String getTcpId();
	
	/**
	 * @author gewx 获取池对象
	 * **/
	ObjectPool<RpcPushDefine> getRpcPoolObject();
	
	/**
	 *@author gewx tcp状态,是否入池[true-已入,false-未入]
	 * **/
	void setState(boolean bool);
	
	/**
	 * @author gewx 获取tcp状态.
	 * **/
	boolean getState();
	
	/**
	 * @author gewx 心跳检测包方法名
	 * **/
	String HEARTBEAT_METHOD = "heartbeat";
}
