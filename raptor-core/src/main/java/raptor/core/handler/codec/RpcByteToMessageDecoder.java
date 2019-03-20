package raptor.core.handler.codec;

import java.util.List;

import org.nustaq.serialization.FSTConfiguration;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author gewx 入站处理-解码器,解决TCP拆包,粘包,半包读写.
 * 
 * **/
public final class RpcByteToMessageDecoder extends ByteToMessageDecoder {

	private static final int INT_BYTE_LEN = 4; 
	
	private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		if (in.readableBytes() < INT_BYTE_LEN) {
			return;
		}
		
		int rpcByteCount = in.readInt(); //RPC调用字节总数

		if (in.readableBytes() < rpcByteCount) {
			in.resetReaderIndex();
			return;
		}

		byte [] rpcByteArray = new byte[rpcByteCount];
		in.readBytes(rpcByteArray);
//		in.discardReadBytes(); //清理已读取后的无效堆外内存空间.[原理是: 时间(CPU)换空间(堆外内存),这行代码可不加].

		Object rpcObject = configuration.asObject(rpcByteArray); //反序列化
		out.add(rpcObject);	
	}

}
