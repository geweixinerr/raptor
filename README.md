# raptor
基于Netty4与Apache Pool2开发的RPC服务中间件,特性如下:<br>
1.长连接设计,支持同步与异步调用,系统稳定坚如磐石 <br>
2.支持吞吐量优先模式/支持低延迟优先模式 <br>
3.支持分布式日志采集[traceId设计]

# 启动
服务端启动分为两种方式:
1.Spring方式 可以在服务端配置文件中设置初始化Bean
```
<bean name="RpcinitBean" class="raptor.core.init.RpcInitBean"/>
```
同时也需要配置 RpcServerConfig,如下:
```
 <!-- RPC服务端配置,NIO线程池大小根据服务器所在物理主机CPU核心数动态拓展,数值设定为: core * 2 -->
<bean id="RpcServerConfig" class="org.springframework.beans.factory.config.MapFactoryBean">
		<property name="targetMapClass">
			<value>java.util.HashMap</value>
		</property>
		
		<property name="sourceMap">
			<map>
			    <entry key="localAddress" value="localhost"/> <!-- 绑定的服务器IP地址 -->
				<entry key="port" value="8090"/> <!-- 服务端口号 -->
			</map>
		</property>    
</bean>  
```
备注:以上配置必须同时出现在系统中,才可以配合启动服务.

2.编码式手工启动方式
```
// 模拟启动Spring,以作测试.
ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");

//手工构建Server参数,启动服务
RpcServerManualBuilder server = new RpcServerManualBuilder.Builder().address("localhost").port(8090)
		.setContext(context).build();
server.start();
```
客户端启动分为两种方式: 
1.Spring方式 可以在服务端配置文件中设置客户端初始化
```
<bean name="RpcinitBean" class="raptor.core.init.RpcInitBean"/>
```
```
<!-- Rpc Client 客户端配置 -->
<bean id="NettyPoolConfig"
	class="org.springframework.beans.factory.config.ListFactoryBean">
	<property name="targetListClass" value="java.util.concurrent.CopyOnWriteArrayList" />
	<property name="singleton" value="false" />
	<property name="sourceList">
		<list>
		    <bean class="org.springframework.beans.factory.config.MapFactoryBean">
				<property name="targetMapClass">
					<value>java.util.HashMap</value>
				</property>
				<property name="sourceMap">
					<map>
					    <entry key="serverNode" value="mc"/>
						<entry key="remote" value="localhost"/> 
						<entry key="port" value="8090"/>
						<entry key="maxclients" value="32"/> 
						<entry key="minclients" value="6"/> 
					</map>
				</property>    
			</bean>  
		    <bean class="org.springframework.beans.factory.config.MapFactoryBean">
				<property name="targetMapClass">
					<value>java.util.HashMap</value>
				</property>
				<property name="sourceMap">
					<map>
					    <entry key="serverNode" value="tcs"/>
						<entry key="remote" value="localhost"/> 
						<entry key="port" value="8090"/>
						<entry key="maxclients" value="32"/> 
						<entry key="minclients" value="6"/> 
					</map>
				</property>    
			</bean>  
		</list>
	</property>
</bean>
```
2.编码式手工启动方式
```
//在类的静态模块里建立与服务器的连接
static {
	RpcClientManualBuilder clientBuilder = new RpcClientManualBuilder.Builder().serverNode("mc").remote("localhost")
			.port(8090).addClient().build();

	try {
		clientBuilder.connection();
	} catch (Exception e) {
		LOGGER.error("客户端初始化异常: " + StringUtil.getErrorText(e));
	}
}
```
备注: 无论采用何种启动方式启动服务端服务,都需要借助Spring容器加载RPC服务方法,具体需要通过RpcHandler注解启用.如下所示:
```
@RpcHandler
@Service
public final class AlibabaService {

	private static final RaptorLogger LOGGER = new RaptorLogger(AlibabaService.class);

	/**登录认证**/
	@RpcMethod
	public String LoginAuth() {
		LOGGER.info("LoginAuth----------------->");
		return "Netty is VeryGood!";
	}
}
```
# 测试
服务器启动： 已eclipse为例，选中raptor-web子模块, 执行Maven插件命令 jetty:run
```
2019-08-10 13:27:20.609||[main] INFO   raptor.core.init.RpcInitBean - 应用服务器启动,RPC服务端参数初始化...
2019-08-10 13:27:20.689||[main] INFO   raptor.core.server.RpcServerTaskPool - 初始化RPC Server业务线程池对象...
2019-08-10 13:27:20.691||[main] INFO   o.s.s.c.ThreadPoolTaskExecutor - Initializing ExecutorService
2019-08-10 13:27:20.724||[main] INFO   raptor.core.server.RpcServer - 非Linux系统下,RPC Server启动...
2019-08-10 13:27:21.340||[nioEventLoopGroup-2-1] INFO   raptor.core.server.RpcServer - RPC 服务启动成功!
2019-08-10 13:27:21.342||[main] INFO   r.core.server.task.RpcServerMonitor - RpcServerMonitor监视器扫描...
2019-08-10 13:27:21.344||[main] INFO   o.s.s.c.ScheduledExecutorFactoryBean - Initializing ExecutorService
2019-08-10 13:27:21.348||[main] INFO   raptor.core.init.RpcInitBean - 应用服务器启动,RPC客户端参数初始化...
2019-08-10 13:27:21.386||[main] INFO   raptor.core.client.RpcClientTaskPool - 初始化RPC Client业务线程池对象...
........
```
客户端连接测试参考: raptor.RaptorClientTest

# raptor中间件核心目录说明:
raptor-core [核心模块,可独立剥离出来。其他的模块是作为常规项目演示如何引用raptor-core使用而创立]

# 其他
raptor为个人研发项目,在研发过程中可能需要兼顾模拟实际的业务场景测试而引入第三方外部资源依赖[MySQL/Redis]等,继而会引入一些配置.
这些配置需要您在下载之后根据实际情况进行修改. 如遇到问题可Issues与我沟通交流,我将感激不尽.

# MySQL数据源配置
raptor\deploy\vars <br>
raptor\raptor-web\src\main\resources\spring\spring-datasource.xml.bak

备注: 需要引入MySQL时将数据源后缀.bak去除即可
