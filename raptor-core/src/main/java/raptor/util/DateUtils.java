package raptor.util;

import org.joda.time.DateTime;

/**
 * 日期工具类
 * 
 * @author gewx
 * **/
public final class DateUtils {

	/**
	 * 计算时间差,RPC请求耗时,单位:毫秒
	 * 
	 * @author gewx 
	 **/
	public static long timeDiffForMilliSecond(DateTime start, DateTime end) {
		return end.getMillis() - start.getMillis();
	}
}
