package raptor.core;

import raptor.core.message.RpcResponseBody;

/**
 * @author gewx RPC服务端业务分发回调抽象类
 **/
public abstract class AbstractCallBack {

	/**
	 * @author gewx 业务回调
	 * @param RpcResult
	 *            result,回调结果对象.
	 * @return void
	 **/
	public abstract void invoke(final RpcResponseBody result);
}
