package raptor.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import raptor.core.client.task.RpcClientMonitor;
import raptor.core.init.RpcParameter;

/**
 * RPC Client 手工配置构建启动类
 * 
 * @author gewx 
 **/
public final class RpcClientManualBuilder {

	private static final int MINCLIENTS = Runtime.getRuntime().availableProcessors();

	private static final int MAXCLIENTS = MINCLIENTS * 2;

	private static final int ZERO = 0;
	
	private final List<Map<String, String>> clientConfig;

	private RpcClientManualBuilder(List<Map<String, String>> clientConfig) {
		this.clientConfig = clientConfig;
	}

	public void connection() throws Exception {
		checkConfig();
		RpcParameter.INSTANCE.initRpcParameter(clientConfig);
		RpcClientTaskPool.initPool();
		RpcClientMonitor.scan();
		RpcClient.connection();
	};

	public static class Builder {

		private String serverNode;

		private String remote;

		private int port;

		private int maxclients;

		private int minclients;

		private final List<Map<String, String>> clientConfig = new ArrayList<>();

		public Builder serverNode(String serverNode) {
			this.serverNode = serverNode;
			return this;
		}

		public Builder remote(String remote) {
			this.remote = remote;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder maxclients(int maxclients) {
			this.maxclients = maxclients;
			return this;
		}

		public Builder minclients(int minclients) {
			this.minclients = minclients;
			return this;
		}

		public Builder addClient() {
			checkClientNode();

			Map<String, String> node = new HashMap<>(16);
			node.put("serverNode", this.serverNode); // 服务节点
			node.put("remote", this.remote); // 服务节点IP地址
			node.put("port", String.valueOf(this.port)); // 端口号
			node.put("maxclients", String.valueOf(this.maxclients == 0 ? MAXCLIENTS : this.maxclients)); // 最大TCP连接数
			node.put("minclients", String.valueOf(this.minclients == 0 ? MINCLIENTS : this.minclients)); // 最小TCP连接数

			clientConfig.add(node);
			return this;
		}

		public RpcClientManualBuilder build() {
			return new RpcClientManualBuilder(clientConfig);
		}

		private void checkClientNode() {
			if (StringUtils.isBlank(this.serverNode)) {
				throw new NullPointerException("Rpc ClientNode Configuration --> serverNode is Empty!");
			}

			if (StringUtils.isBlank(this.remote)) {
				throw new NullPointerException("Rpc ClientNode Configuration --> remote is Empty!");
			}

			if (port == ZERO) {
				throw new NullPointerException("Rpc ClientNode Configuration --> port is Empty!");
			}
		}
	}

	private void checkConfig() {
		if (CollectionUtils.isEmpty(clientConfig)) {
			throw new NullPointerException("Rpc Client Configuration is Empty!");
		}
	}
}
