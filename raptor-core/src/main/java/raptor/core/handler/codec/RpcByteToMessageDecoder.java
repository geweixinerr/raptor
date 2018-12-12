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
		in.markReaderIndex();
		if (in.readableBytes() < 4) {
			return;
		}
		
		int rpcByteCount = in.readInt(); //RPC调用字节总数

		if (in.readableBytes() < rpcByteCount) {
			in.resetReaderIndex();
			return;
		}

		byte [] rpcByteArray = new byte[rpcByteCount];
		in.readBytes(rpcByteArray);
//		in.discardReadBytes(); //清理无效堆外内存.时间换CPU空间[这行代码可不加.]

		Object rpcObject = configuration.asObject(rpcByteArray); //反序列化
		out.add(rpcObject);	
	}

}
