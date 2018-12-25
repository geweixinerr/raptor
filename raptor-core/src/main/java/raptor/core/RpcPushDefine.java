package raptor.core;

import raptor.core.message.RpcRequestBody;

/**
 * @author gewx RPC服务接口定义. Channel与业务服务方法解耦层.
 * **/
public interface RpcPushDefine {

	/**
	 * @author gewx 消息推送.
	 * @param RpcRequestBody RPC请求对象
	 * @return 服务请求受理结果, true : 受理成功, false: 受理失败,服务拒绝[超过raptor中间件发送的数据包上限,参考属性: ChannelOption.WRITE_BUFFER_WATER_MARK]
	 * **/
	boolean pushMessage(RpcRequestBody requestBody);
	
	/**
	 * @author gewx 资源关闭
	 * **/
	void close();
}
