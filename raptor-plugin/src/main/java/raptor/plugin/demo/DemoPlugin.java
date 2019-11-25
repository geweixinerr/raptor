package raptor.plugin.demo;

import org.springframework.stereotype.Service;

import raptor.RaptorRpc;
import raptor.core.RpcResult;
import raptor.core.message.RpcResponseBody;
import raptor.exception.RpcException;
import raptor.log.RaptorLogger;

/**
 * 插件层案例
 * 
 * @author gewx
 **/

@Service
public final class DemoPlugin {

	private static final RaptorLogger LOGGER = new RaptorLogger(DemoPlugin.class);

	private static final String SERVER_NODE = "tcs";

	@SuppressWarnings("rawtypes")
	private static final RaptorRpc RPC = new RaptorRpc();

	/**
	 * 插件层不做任务异常逻辑处理,只做数据传输,由service层根据业务决定是否处理. 示例
	 * 
	 * @author gewx
	 * @throws RpcException 通讯异常
	 * @return 数据对象
	 **/
	@SuppressWarnings("unchecked")
	public Object casLonginAuth() throws RpcException {
		String methodName = "casLonginAuth";
		LOGGER.enter(methodName, "DEMO插件调用[start]");
		RpcResponseBody response = RPC.sendSyncMessage(SERVER_NODE, "loginAuth");
		LOGGER.info("RPC响应: " + response);
		Object result = null;
		if (RpcResult.SUCCESS.equals(response.getRpcCode())) {
			result = response.getBody();
		}
		LOGGER.exit(methodName, "DEMO插件调用[end], result: " + result);
		return result;
	}
}
