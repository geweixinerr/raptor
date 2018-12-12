package raptor.core.client;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端测试数据,1KB
 * **/
public final class TestData {

	public static final Map<String,String> data = new HashMap<String,String>();
	
	static {
		data.put("Message","Netty之有效规避内存泄漏\r\n" + 
				"有过痛苦的经历，特别能写出深刻的文章 —— 凯尔文. 肖直接内存是IO框架的绝配，但直接内存的分配销毁不易，所以使用内存池能大幅提高性能，也告别了频繁的GC。\r\n" + 
				"但要重新培养被Java的自动垃圾回收惯坏了的惰性。Netty有一篇必读的文档 官方文档翻译：引用计数对象 ，在此基础上补充一些自己的理解和细节。\r\n" + 
				"1.为什么要有引用计数器Netty里四种主力的ByteBuf，其中UnpooledHeapByteBuf 底下的byte[]能够依赖JVM GC自然回收；而UnpooledDirectByteBuf底下是DirectByteBuffer，\r\n" + 
				"如Java堆外内存扫盲贴所述，除了等JVM GC，最好也能主动进行回收；而PooledHeapByteBuf 和 PooledDirectByteBuf，\r\n" + 
				"则必须要主动将用完的byte[]/ByteBuffer放回池里，否则内存就要爆掉。Netty is Very Good! ");
	}
}
