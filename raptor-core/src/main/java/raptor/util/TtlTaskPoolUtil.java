package raptor.util;

import java.util.concurrent.Callable;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;

import raptor.core.Constants;

public final class TtlTaskPoolUtil {
	
	public static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<String>();
	
	public static final TransmittableThreadLocal<String> t_threadLocal = new TransmittableThreadLocal<String>();
	
	private static final ThreadPoolTaskExecutor POOLTASKEXECUTOR = new ThreadPoolTaskExecutor();
	
	static {
		POOLTASKEXECUTOR.setQueueCapacity(Constants.CPU_CORE * 1024);
		POOLTASKEXECUTOR.setCorePoolSize(1);
		POOLTASKEXECUTOR.setMaxPoolSize(1);
		POOLTASKEXECUTOR.setThreadNamePrefix("TASK_RPC_CLIENT_");
		POOLTASKEXECUTOR.initialize();

		POOLTASKEXECUTOR.getThreadPoolExecutor().prestartAllCoreThreads();
	}
	
	public static void main(String[] args) {
		threadLocal.set("Java");
		t_threadLocal.set("Java");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("初始==================================>");
				System.out.println("threadId: " + threadLocal.get());
				System.out.println("t_threadLocal: " + t_threadLocal.get());
			}
		}).start();
		
		POOLTASKEXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("第一次==================================>");
				System.out.println("threadId: " + threadLocal.get());
				System.out.println("t_threadLocal: " + t_threadLocal.get());
			}
		});
		
		POOLTASKEXECUTOR.execute(TtlRunnable.get(new Runnable() {
			@Override
			public void run() {
				System.out.println("第二次==================================>");
				System.out.println("threadId: " + threadLocal.get());
				System.out.println("t_threadLocal: " + t_threadLocal.get());
			}
		}));
		
		POOLTASKEXECUTOR.submit(TtlCallable.get(new Callable<String>() {
			@Override
			public String call() throws Exception {
				System.out.println("第三次==================================>");
				System.out.println("threadId: " + threadLocal.get());
				System.out.println("t_threadLocal: " + t_threadLocal.get());
				return null;
			}
		}));
		
		TtlExecutors.getTtlExecutor(POOLTASKEXECUTOR).execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("第四次==================================>");
				System.out.println("threadId: " + threadLocal.get());
				System.out.println("t_threadLocal: " + t_threadLocal.get());
			}
		});
		
		TtlExecutors.getTtlExecutor(POOLTASKEXECUTOR).execute(new Runnable() {
			@Override
			public void run() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println("第五次==================================>");
						System.out.println("threadId: " + threadLocal.get());
						System.out.println("t_threadLocal: " + t_threadLocal.get());
					}
				}).start();
			}
		});
		
	}
}
