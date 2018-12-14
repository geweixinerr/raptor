package raptor.core.client;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import raptor.core.RpcPushDefine;

/**
 * @author gewx RPC服务客户端服务注册表.
 **/
public final class RpcClientRegistry {

	public static final RpcClientRegistry INSTANCE = new RpcClientRegistry();

	/**
	 * 注册表KEY枚举
	 **/
	public enum rpcEnum {
		rpcPushDefine
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Map<Enum<rpcEnum>, RpcPushDefine> REGISTRY_MAP = Collections
			.synchronizedMap(new EnumMap(rpcEnum.class));

	private RpcClientRegistry() {
	}

	/**
	 * @author gewx 注册RpcPushDefine实现实例
	 * @param Enum
	 *            e-枚举Key, RpcPushDefine handler-事件处理实现channelHandler
	 * @return void
	 **/
	public void registry(Enum<rpcEnum> e, RpcPushDefine handler) {
		REGISTRY_MAP.put(e, handler);
	}
	
	/**
	 * @author gewx 获取注册表对象
	 * **/
	public RpcPushDefine getRpcClient(Enum<rpcEnum> e) {
		return REGISTRY_MAP.get(e);
	}
}
