
# 概述
	
	设计目标：
		发布和订阅消息
		存储并容错消息
		快速及时处理流数据
	
	相关概念：
		主题（Topic）
		消息（Record）
		分区和副本（partition & replica）
		主从副本（leader replica & follower replica）
		偏移量（offset, 新版本默认保存在__consumer_offsets主题中，老版本默认保存在zk中）
		日志段（log segment, 一个日志段对应物理文件 ${log.dirs}/topicname-{num}/x.log x.index x.timeindex ）	
		
		代理（Broker, 每一个kafka实例都有一个唯一的brokerId）
		生产者（producer）
		消费者和消费者组（默认组test-consumer-group, 默认消费者id ${groupid}-${host}-${timestamp}-${UUID前8位} ）
		ISR（in-sync replica）保存与leader副本同步的副本列表，如果一个follower宕机，则该follower replica节点将从ISR列表中移除
		Zookeeper（保存代理节点、kafka集群、消息主题和分区状态、分区副本分配、动态分配等信息）
		
	主要特性：
		消息持久化（6块7200r/min SATA RAID-5阵列磁盘线性写的速度为600M/s，而随机写只有100K/s，kafka即采用消息顺序追加写方式持久化消息）
		高吞吐（利用磁盘顺序读写、zero-copy、数据压缩[gzip/snappy/lz4]、主题分区处理等技术，使kafka能够支持 百万/秒 的消息处理速度）
		高可用（leader replica & follower replica, record persistence）
		易展性（使用zk作为协调者，使得producer、consumer、broker[stateless] 都可以为分布式配置，在线自动感知及负载均衡）
		多语言支持（核心模块使用scala实现，支持多种开发语言，能够和当前主流大数据框架很好地集成）
		Kafka Streams（0.10版本后引入的流处理API）
		
	应用场景：
		消息系统：高吞吐、高可用、易扩展消息处理平台
		应用监控：利用kafka采集应用程序和服务器健康相关的指标（CPU占用率、IO、内存、连接数、TPS、QPS等），对指标进行处理，
			    构建仪表盘或曲线图等可视化监控系统。例如跟多公司采用kafka与ELK整合构建应用服务监控系统。
		网站用户行为跟踪：将用户操作轨迹、浏览内容等信息发送到kafka集群上，通过hadoop、spark、storm等对分析数据、生成统计报表。
	

# 安装配置
	
	环境准备:
		jdk1.7+, zookeeper3.4.5+, ssh login without password
	
	zk配置文件:
		tickTime=2000
		initLimit=10
		syncLimit=5
		dataDir=/var/lib/zookeeper
		dataLogDir=/var/lib/zookeeper
		clientPort=2181
		##
		server.1=server-1:2888:3888		
		server.2=server-2:2888:3888		
		server.3=server-3:2888:3888		
	
	kafka配置文件:
	$ bin/kafka-server-start.sh config/server.properties
		## Server Basics
		broker.id=0
		delete.topic.enable=true
		listeners=PLAINTEXT://:9092
		advertised.listeners=PLAINTEXT://your.host.name:9092
		
		## Socket Server Settings
		num.network.threads=3
		num.io.threads=8
		socket.send.buffer.bytes=102400
		socket.receive.buffer.bytes=102400
		socket.request.max.bytes=104857600
		
		## Log Basics & Log Flush and Retention Policys
		log.dirs=/tmp/kafka-logs
		num.partitions=1
		num.recovery.threads.per.data.dir=1
		log.flush.interval.messages=10000
		log.flush.interval.ms=1000
		log.retention.hours=168
		log.retention.bytes=1073741824
		log.segment.bytes=1073741824
		log.retention.check.interval.ms=300000
		
		##Zookeeper
		zookeeper.connect=host1:2181,host2:2181,host3:2181/kafka
		zookeeper.connection.timeout.ms=6000
		
	kakfa manager安装：
		a) 下载
			$ git clone https://github.com/yahoo/kafka-manager
		b) 解压编译
			$ ./sbt clean dist
		c) 修改配置
			$ vim conf/application.conf
				kafka-manager.zkhosts="hadoop01:2181,hadoop02:2181,hadoop08:2181/kafka80"
		d) 启动
			$ nohup ./kafka-manager -Dconfig.file=../conf/application.conf &
			$ nohup ./kafka-manager -Dconfig.file=../conf/application.conf -Dhttp.port=9000 &
		e) 访问
			curl hostname:9000
		f) 查看进程
			$ jps
			  17845 ProdServerStart
		注意：kafkaManager运行时有一个类似锁的文件RUNNING_PID，位于bin同级目录下，为了不影响下次启动，
		在执行kill杀死进程命令后，需要删除RUNNING_PID文件，否则在下次启动时会由于进程占用而无法启动。
		
	
# 核心组件

	延迟操作组件
	
	控制器
	
	协调器
	
	网络通信器
	
	日志管理器
	
	副本管理器
	
	处理器
	
	动态配置管理器
	
	内部监控器

# 工作机制

	KafkaServer启动流程分期

	创建Topic流程分析

	生产者

	消费者
	

# 基本操作
	KafkaServer管理
	
	Topic管理
	
	生产者基本操作
	
	消费者基本操作
	
	配置管理
	
	分区操作
	
	连接器基本操作
	
	Kafka Manager应用
	
	Kafka安全机制
	
	镜像操作
	

# 基本API
	主题管理
	生产者
	消费者
	自定义组件
		分区器
		序列化和反序列化器
	与spring整合使用
	

# Streams API
	基本概念：
		流
		流处理器
		处理器拓扑
		时间
		状态
		KStream和KTable
		窗口
	基本接口：
		KStream和KTable
		窗口操作
		连接操作
		变换操作
		聚合操作


# 数据采集应用
	集成Log4j
	集成Flume
	

# 与ELK整合
	
