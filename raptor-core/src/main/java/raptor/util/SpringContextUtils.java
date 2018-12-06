package raptor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author Administrator Spring上下文启动初始化类
 * **/
@Service
public final class SpringContextUtils implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextUtils.class);
	
	private ApplicationContext context;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {	
		LOGGER.info("Spring上下文注入!");
		this.context = applicationContext;
	}

	/**
	 * 输出实例
	 * **/
	public ApplicationContext getApplicationContextInstance() {		
		return context;
	}
	
}
