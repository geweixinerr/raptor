package raptor.core.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 客户端测试数据,1KB
 **/
public final class NettyTestData implements Serializable {

	private static final long serialVersionUID = -1534161005217380696L;

	private Integer age;

	private String userName;

	private String address;

	private float money_0;

	private double money_1;

	private byte b;

	private boolean bool;

	private int i;

	private long count;

	private final Map<String, String> map;

	private final List<String> list;

	private final String[] array;

	public NettyTestData() {
		this.age = 32;
		this.userName = "geweixin";
		this.address = "江苏镇江";
		this.money_0 = 120.2f;
		this.money_1 = 1221.01d;
		this.b = '1';
		this.bool = true;
		this.i = 100;
		this.count = 10000L;
		this.map = new HashMap<String, String>();
		this.map.put("Message", "Netty之有效规避内存泄漏\r\n"
				+ "有过痛苦的经历，特别能写出深刻的文章 —— 凯尔文. 肖直接内存是IO框架的绝配，但直接内存的分配销毁不易，所以使用内存池能大幅提高性能，也告别了频繁的GC。\r\n"
				+ "但要重新培养被Java的自动垃圾回收惯坏了的惰性。Netty有一篇必读的文档 官方文档翻译：引用计数对象 ，在此基础上补充一些自己的理解和细节。\r\n"
				+ "1.为什么要有引用计数器Netty里四种主力的ByteBuf，其中UnpooledHeapByteBuf 底下的byte[]能够依赖JVM GC自然回收；而UnpooledDirectByteBuf底下是DirectByteBuffer，\r\n"
				+ "如Java堆外内存扫盲贴所述，除了等JVM GC，最好也能主动进行回收；而PooledHeapByteBuf 和 PooledDirectByteBuf，\r\n"
				+ "则必须要主动将用完的byte[]/ByteBuffer放回池里，否则内存就要爆掉。");

		this.list = new ArrayList<String>();
		this.list.add("Netty is Very Good!");
		this.array = new String[] { "Netty", "is", "Good!" };
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public float getMoney_0() {
		return money_0;
	}

	public void setMoney_0(float money_0) {
		this.money_0 = money_0;
	}

	public double getMoney_1() {
		return money_1;
	}

	public void setMoney_1(double money_1) {
		this.money_1 = money_1;
	}

	public byte getB() {
		return b;
	}

	public void setB(byte b) {
		this.b = b;
	}

	public boolean isBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public List<String> getList() {
		return list;
	}

	public String[] getArray() {
		return array;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024 + 512);
		ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE, sb);

		builder.append("age", this.age);
		builder.append("userName", this.userName);
		builder.append("address", this.address);
		builder.append("money_0", this.money_0);
		builder.append("money_1", this.money_1);
		builder.append("b", this.b);
		builder.append("bool", this.bool);
		builder.append("i", this.i);
		builder.append("map", this.map);
		builder.append("list", this.list);
		builder.append("array", ArrayUtils.toString(this.array));

		sb.trimToSize();
		return builder.toString();
	}

}
