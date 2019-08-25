package raptor.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * @author gewx 字符串操作辅助类
 **/

public final class StringUtil {

	private StringUtil() {
	}

	/**
	 * @author gewx 异常栈字符串输出
	 * @param Throwable ex
	 * @return String
	 **/
	public static String getErrorText(Throwable throwable) {
		if (throwable == null) {
			return "ERROR,throwable is NULL!";
		}

		try (StringWriter strWriter = new StringWriter(512); PrintWriter writer = new PrintWriter(strWriter)) {
			throwable.printStackTrace(writer);
			StringBuffer sb = strWriter.getBuffer();
			return sb.toString();
		} catch (Exception ex) {
			return "ERROR!";
		}
	}

	/**
	 * @author gewx 计算时间差,RPC请求耗时,单位:毫秒
	 **/
	public static Integer timeDiffForMilliSecond(DateTime date1, DateTime date2) {
		Period p2 = new Period(date1, date2);
		int seconds = p2.getSeconds(); // 相差的秒
		return seconds * 1000 + p2.getMillis();
	}
}
