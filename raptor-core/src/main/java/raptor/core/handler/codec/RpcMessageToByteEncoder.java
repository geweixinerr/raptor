package raptor.core.handler.codec;

import org.nustaq.serialization.FSTConfiguration;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import raptor.core.message.RpcMessage;

/**
 * @author gewx 出站处理-编码器
 * **/
public final class RpcMessageToByteEncoder extends MessageToByteEncoder<RpcMessage> {

	/**
	 * 得益于Netty的IO线程设计[线程封闭技术],下面的代码不会存在线程安全问题.
	 * **/
	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();
	
	@Override
	protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
		byte [] rpcByteArray = configuration.asByteArray(msg);
		int rpcByteCount = rpcByteArray.length;
		out.writeInt(rpcByteCount);
		out.writeBytes(rpcByteArray);
	}

}
