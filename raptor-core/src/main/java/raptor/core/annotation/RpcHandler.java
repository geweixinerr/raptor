package raptor.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC处理器注解
 * 
 * @author gewx
 **/
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcHandler {

	/**
	 * RPC类名,契约
	 * **/
	String value() default "";
}
