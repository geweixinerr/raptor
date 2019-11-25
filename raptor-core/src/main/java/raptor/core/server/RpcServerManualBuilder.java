package raptor.core.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import raptor.core.annotation.RpcHandler;
import raptor.core.init.RpcMapping;
import raptor.core.init.RpcParameter;
import raptor.core.server.task.RpcServerMonitor;

/**
 * @author gewx RPC Server 手工配置构建启动类
 **/
public final class RpcServerManualBuilder {

	private static final String ADDRESS_KEY = "localAddress";

	private static final String PORT = "port";
	
	private static final int ZERO = 0;

	private final Map<String, String> CONFIG_MAP;

	private final ApplicationContext context;

	private RpcServerManualBuilder(String address, int port, ApplicationContext context) {
		CONFIG_MAP = new HashMap<>();
		CONFIG_MAP.put(ADDRESS_KEY, address);
		CONFIG_MAP.put(PORT, String.valueOf(port));
		this.context = context;
	}

	public void start() throws InterruptedException {
		checkConfig();

		RpcParameter.INSTANCE.initRpcParameter(CONFIG_MAP);
		RpcServerTaskPool.initPool();
		RpcServer.start();
		RpcServerMonitor.scan();

		if (context != null) {
			Map<String, Object> rpcMap = context.getBeansWithAnnotation(RpcHandler.class);
			RpcMapping.initRpcMapping(rpcMap);
		}
	};

	public static class Builder {

		private String address;

		private int port;

		private ApplicationContext context;

		public Builder address(String address) {
			this.address = address;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder setContext(ApplicationContext context) {
			this.context = context;
			return this;
		}

		public RpcServerManualBuilder build() {
			return new RpcServerManualBuilder(this.address, this.port, this.context);
		}
	}

	private void checkConfig() {
		if (StringUtils.isBlank(CONFIG_MAP.get(ADDRESS_KEY))) {
			throw new NullPointerException("Rpc Server Configuration --> localAddress is Empty!");
		}

		if (Integer.parseInt(CONFIG_MAP.get(PORT)) == ZERO) {
			throw new NullPointerException("Rpc Server Configuration --> port is Empty!");
		}
	}
}
