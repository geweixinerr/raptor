package raptor.core;

import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;

/**
 * @author gewx RPC服务端业务分发回调抽象类
 **/
public abstract class AbstractCallBack {

	/**
	 * @author gewx 业务回调
	 * @param RpcResponseBody
	 *            resp,回调结果对象.
	 * @return void
	 **/
	public abstract void invoke(final RpcResponseBody resp);

	/**
	 * @author gewx 业务回调 (测试专用)
	 * @param RpcRequestBody
	 *            req,请求数据, RpcResponseBody respo 响应回调对象.
	 * @return void
	 **/
	public void invoke(final RpcRequestBody req, final RpcResponseBody resp) {}
}
