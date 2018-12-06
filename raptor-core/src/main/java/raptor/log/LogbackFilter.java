package raptor.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * 日志拦截过滤器<br> 
 * logaback日志拦截
 *
 * @author gewx
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public final class LogbackFilter extends Filter<ILoggingEvent> {

	//日志等级
	enum LEVEL{
		DEBUG,INFO,WARN,ERROR
	};
	
	/**
	 * FilterReply返回值定义
	 * 返回值为DENY时，日志事件直接丢弃这条日志，不会再传递给剩下的过滤器。
	 * 返回值为NEUTRAL时，则传递给下面的过滤器。
	 * 返回值为ACCEPT时，日志事件立即处理这条日志，跳过调用其它过滤器。
	 * 
	 * **/
	@Override
	public FilterReply decide(ILoggingEvent event) {
		//SQL语句里打印等级为DEBUG,只拦截DEBUG级日志.
		if (event.getLevel().toString().equals(LEVEL.DEBUG.toString())) {
			//SELECT/INSERT/UPDATE/INSERT
			String message = event.getMessage();
			if (message.contains("Preparing") || message.contains("Parameters") ||
					message.contains("Total")) {
				return FilterReply.ACCEPT; // 允许输出
			} else {
				return FilterReply.DENY; // 不允许输出
			}
		} else {
			return FilterReply.ACCEPT; // 允许输出
		}
	}
	
}

