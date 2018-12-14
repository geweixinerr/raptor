package raptor.core;

import raptor.core.message.RpcRequestBody;

/**
 * @author gewx RPC服务接口定义. Channel与业务服务方法解耦层.
 * **/
public interface RpcPushDefine {

	/**
	 * @author gewx 消息推送.
	 * @param RpcRequestBody RPC请求对象
	 * @return void
	 * **/
	void pushMessage(RpcRequestBody requestBody);
}
