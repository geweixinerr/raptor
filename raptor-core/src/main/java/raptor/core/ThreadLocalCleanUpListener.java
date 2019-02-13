package raptor.core;

import java.lang.ref.Reference;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * @author gewx ThreadLocal清理监听器
 * <listener>
 *    <listener-class>raptor.core.ThreadLocalCleanUpListener</listener-class>
 * </listener>
 * 
 * 问题描述与解决:
 * https://stackoverflow.com/questions/3869026/how-to-clean-up-threadlocals
 * **/

public final class ThreadLocalCleanUpListener implements ServletRequestListener {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void cleanUpThreadLocals() throws Exception {
		// Get a reference to the thread locals table of the current thread
		Thread thread = Thread.currentThread();
		Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
		threadLocalsField.setAccessible(true);
		Object threadLocalsInThread = threadLocalsField.get(thread);

		// Get a reference to the array holding the thread local variables
		// inside the
		// ThreadLocalMap of the current thread
		Class threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");

		Method removeInThreadLocalMap = threadLocalMapClass.getDeclaredMethod("remove", ThreadLocal.class);
		removeInThreadLocalMap.setAccessible(true);

		Field tableField = threadLocalMapClass.getDeclaredField("table");
		tableField.setAccessible(true);
		Object table = tableField.get(threadLocalsInThread);

		for (int i = 0; i < Array.getLength(table); i++) {
			// Each entry in the table array of ThreadLocalMap is an Entry
			// object
			// representing the thread local reference and its value
			Object entry = Array.get(table, i);
			Method getMethod = Reference.class.getDeclaredMethod("get");
			if (entry != null) {
				ThreadLocal threadLocal = (ThreadLocal) getMethod.invoke(entry);
				removeInThreadLocalMap.invoke(threadLocalsInThread, threadLocal);
			}
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent paramServletRequestEvent) {
		try {
			cleanUpThreadLocals();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void requestInitialized(ServletRequestEvent paramServletRequestEvent) {
		
	}

}
