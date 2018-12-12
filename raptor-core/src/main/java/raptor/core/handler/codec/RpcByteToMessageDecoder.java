package raptor.core.handler.codec;

import java.util.List;

import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author gewx 入站处理-解码器,转为RpcRequestBody
 * 解决TCP拆包,粘包,半包读写
 * **/
public final class RpcByteToMessageDecoder extends ByteToMessageDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcByteToMessageDecoder.class);

	/**
	 * 得益于Netty的IO线程设计[线程封闭技术],下面的代码不会存在线程安全问题.
	 * **/
	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		LOGGER.info("数据解码!");
		int rpcByteCount = 0;
		if (in.readableBytes() > 4) {
			rpcByteCount = in.readInt(); //RPC调用字节总数
		}
		
		if (in.readableBytes() < rpcByteCount) {
			return;
		}

		byte [] rpcByteArray = new byte[in.readableBytes()];
		in.readBytes(rpcByteArray);
		
		Object rpcObject = configuration.asObject(rpcByteArray); //反序列化
		out.add(rpcObject);
	}

}
