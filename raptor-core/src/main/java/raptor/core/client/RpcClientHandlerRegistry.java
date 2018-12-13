package raptor.core.client;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import io.netty.channel.ChannelHandler;

/**
 * @author gewx RPC服务客户端服务注册表.
 **/
public final class RpcClientHandlerRegistry {

	public static final RpcClientHandlerRegistry INSTANCE = new RpcClientHandlerRegistry();

	/**
	 * 注册表KEY枚举
	 **/
	public enum classEnum {
		ClientDispatcherHandler
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final Map<Enum<classEnum>, ChannelHandler> REGISTRY_MAP = Collections
			.synchronizedMap(new EnumMap(classEnum.class));

	private RpcClientHandlerRegistry() {
	}

	/**
	 * @author gewx 注册channelHander实例
	 * @param Enum
	 *            e-枚举Key, ChannelHandler handler-事件处理channelHandler
	 * @return void
	 **/
	public void registry(Enum<classEnum> e, ChannelHandler handler) {
		REGISTRY_MAP.put(e, handler);
	}
	
	/**
	 * @author gewx 获取只读注册表对象
	 * **/
	public ChannelHandler getClientHandler(Enum<classEnum> e) {
		return REGISTRY_MAP.get(e);
	}
}
