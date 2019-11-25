package raptor.core;

import raptor.core.message.RpcRequestBody;
import raptor.core.message.RpcResponseBody;

/**
 * RPC业务分发回调抽象类
 * 
 * @author gewx
 **/
public abstract class AbstractCallBack {

	/**
	 * 业务回调
	 * 
	 * @author gewx
	 * @param resp,回调结果对象.
	 * @return void
	 **/
	public abstract void invoke(final RpcResponseBody resp);

	/**
	 * 业务回调 [推荐测试专用]
	 * 
	 * @author gewx
	 * @param req 请求数据, resp 响应回调对象.
	 * @return void
	 **/
	public void invoke(final RpcRequestBody req, final RpcResponseBody resp) {
	}
}
