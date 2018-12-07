package raptor.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author gewx 字符串操作辅助类
 **/

public final class StringUtil {

	private StringUtil() {
	}

	/**
	 * @author gewx 异常栈字符串输出
	 * @param Throwable
	 *            ex
	 * @return String
	 **/
	public static String getErrorText(Throwable throwable) {
		if (throwable == null) {
			return "ERROR,throwable is NULL!";
		}

		try {
			PrintWriter writer = null;
			try {
				StringWriter strWriter = new StringWriter(512);
				writer = new PrintWriter(strWriter);
				throwable.printStackTrace(writer);

				StringBuffer sb = strWriter.getBuffer();
				sb.trimToSize();
				return sb.toString();
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		} catch (Exception ex) {
			return "ERROR!";
		}
	}

}
