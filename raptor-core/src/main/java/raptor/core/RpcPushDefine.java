package raptor.core;

import org.apache.commons.pool2.ObjectPool;

import raptor.core.message.RpcRequestBody;
import raptor.exception.RpcException;

/**
 * RPC服务接口定义. Channel与业务服务方法解耦层.
 * 
 * @author gewx
 **/
public interface RpcPushDefine {

	/**
	 * 消息推送.
	 * 
	 * @author gewx
	 * @param requestBody RPC请求对象
	 * @throws RpcException RPC异常
	 * @return void
	 **/

	void pushMessage(RpcRequestBody requestBody) throws RpcException;

	/**
	 * 资源关闭,释放tcp pool空闲对象.
	 * 
	 * @author gewx
	 **/
	void close();

	/**
	 * 检测tcp pool当中tcp对象,是否可写.
	 * 
	 * @author gewx
	 * @return true 可写, false不可写
	 */
	boolean isWritable();

	/**
	 * 获取tcp连接唯一Id
	 * 
	 * @author gewx
	 * @return tcpId
	 **/
	String getTcpId();

	/**
	 * 获取池对象
	 * 
	 * @author gewx
	 * @return rpc池对象
	 **/
	ObjectPool<RpcPushDefine> getRpcPoolObject();

	/**
	 * pool clean
	 * 
	 * @author gewx
	 **/
	void returnClean();

	/**
	 * 心跳检测包方法名
	 **/
	String HEARTBEAT_METHOD = "heartbeat";
}
