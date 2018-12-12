package raptor.core.handler.codec;

import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import raptor.core.message.RpcMessage;

/**
 * @author gewx 出站处理-编码器
 * **/
public final class RpcMessageToByteEncoder extends MessageToByteEncoder<RpcMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcMessageToByteEncoder.class);

	/**
	 * 得益于Netty的IO线程设计[线程封闭技术],下面的代码不会存在线程安全问题.
	 * **/
	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();
	
	@Override
	protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
		out.retain(); //引用计数器+1.

		byte [] rpcByteArray = configuration.asByteArray(msg);
		int rcpByteCount = rpcByteArray.length;
		out.writeInt(rcpByteCount);
		out.writeBytes(rpcByteArray);
	}

}
