# raptor
基于Netty4与Apache Pool2开发的RPC服务中间件,特性如下:<br>
1.长连接设计,支持同步与异步调用,系统稳定坚如磐石 <br>
2.支持吞吐量优先模式/支持低延迟优先模式 <br>
3.支持分布式日志采集[traceId设计]

# 项目地址访问
http://localhost:9090/raptor/

# 阿里监控
http://localhost:9090/raptor/druid/index.html

# 客户端测试
参考: raptor.RaptorClientTest

# 备注
raptor为个人研发项目,在研发过程中可能需要兼顾模拟实际的业务场景测试而引入第三方外部资源依赖[MySQL/Redis]等,继而会引入一些配置.
这些配置需要您在下载之后根据实际情况进行修改. 如遇到问题可issue与我沟通交流,我将感激不尽.

# MySQL数据源配置
raptor\deploy\vars
raptor\raptor-web\src\main\resources\spring\spring-datasource.xml.bak

备注: 需要引入MySQL时将数据源后缀.bak去除即可
