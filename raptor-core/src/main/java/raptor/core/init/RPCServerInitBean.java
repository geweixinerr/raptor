package raptor.core.init;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author gewx RPC服务端初始化-Init.
 * **/

@Service
public final class RPCServerInitBean implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(RPCServerInitBean.class);
	
	private ApplicationContext context;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {	
		LOGGER.info("Spring上下文注入!");
		this.context = applicationContext;
		
		List list1 = (List) context.getBean("listBean");
		List list2 = (List) context.getBean("listBean");

		System.out.println(list1.equals(list2));
	}

	/**
	 * 输出实例
	 * **/
	public ApplicationContext getApplicationContextInstance() {		
		return context;
	}
	
}
