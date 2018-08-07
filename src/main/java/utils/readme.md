
### kafka在zookeeper中的存储结构:

	a) topic注册信息:
	get /brokers/topics/topicxxx
	Schema:
	{
	    "version": "版本编号目前固定为数字1",
	    "partitions": {
	        "partitionId编号": [
	            同步副本组brokerId列表
	        ],
	        "partitionId编号": [
	            同步副本组brokerId列表
	        ],
	        .......
	    }
	}
	Example:
	{
	    "version": 1,
	    "partitions": {
	    "0":[0,1,2]
	     }
	}
	
	
	b) partition状态信息
	get /brokers/topics/topicxxx/partitions/0/state   //其中0表示partition分区号0
	Schema:
	{
	    "controller_epoch": 表示kafka集群中的中央控制器选举次数,
	    "leader": 表示该partition选举leader的brokerId,
	    "version": 版本编号默认为1,
	    "leader_epoch": 该partition leader选举次数,
	    "isr": [同步副本组brokerId列表]
	}
	Example:
	{
	    "controller_epoch":20,
	    "leader":0,
	    "version":1,
	    "leader_epoch":0,
	    "isr":[0,1,2]
	}
	
	
	c) broker注册信息
	get /brokers/ids/[0...N]    //每个broker的配置文件中都需要指定一个数字类型的id(全局不可重复)，此节点为临时znode
	Schema:
	{
	    "jmx_port": jmx端口号,
	    "timestamp": kafka broker初始启动时的时间戳,
	    "host": 主机名或ip地址,
	    "version": 版本编号默认为1,
	    "port": kafka broker的服务端端口号,由server.properties中参数port确定
	}
	Example:
	{
	    "jmx_port":1,
	    "timestamp":"1452068227537",
	    "host":"h1",
	    "version":1,
	    "port":9092
	}
	
	
	d) controller epoch
	get /controller_epoch  ==>  int (epoch)   
	此值为一个数字，kafka集群中第一个broker第一次启动时为1，以后只要集群中center controller中央控制器所在broker变更或挂掉，
	会重新选举新的center controller，每次center controller变更controller_epoch值就会 + 1; 
	
	
	e) controller注册信息
	get /controller       //存储center controller中央控制器所在kafka broker的信息
	Schema:
	{
	    "version": 版本编号默认为1,
	    "brokerid": kafka集群中broker唯一编号,
	    "timestamp": kafka broker中央控制器变更时的时间戳
	}
	Example:
	{
	    "version":1,
	    "brokerid":160,
	    "timestamp":"1452068227409"
	}
	
	
	f) consumer注册信息
	get /consumers/groupIdxxx/ids/consumerIdStringxxx 
	每个consumer都有一个唯一的ID(consumerId可以通过配置文件指定,也可以由系统生成),此id用来标记消费者信息.
	这是一个临时的znode,此节点的值为请看consumerIdString产生规则,即表示此consumer目前所消费的topic+partitions列表. 
	
	consumerId产生规则：
	   StringconsumerUuid = null;
	   if(config.consumerId!=null && config.consumerId){
	       consumerUuid = consumerId;
	   } else {
	       String uuid = UUID.randomUUID()
	   	  consumerUuid = "%s-%d-%s".format(
	            InetAddress.getLocalHost.getHostName, System.currentTimeMillis,
	            uuid.getMostSignificantBits().toHexString.substring(0,8));
	   }
	   String consumerIdString = config.groupId + "_" + consumerUuid; 
	
	Schema:
	{
	    "version": 版本编号默认为1,
	    "subscription": { //订阅topic列表
	        "topic名称": consumer中topic消费者线程数
	    },
	    "pattern": "static",
	    "timestamp": "consumer启动时的时间戳"
	}
	Example:
	{
	    "version":1,
	    "subscription":{
	        "replicatedtopic":1
	    },
	    "pattern":"white_list",
	    "timestamp":"1452134230082"
	}
	
	
	g) consumer owner
	get /consumers/groupIdxxx/owners/topicxxx/partitionIdxxx  ->  consumerIdString+threadId索引编号
	
	当consumer启动时,所触发的操作:
	* 首先进行"Consumer Id注册";
	* 然后在"Consumer id 注册"节点下注册一个watch用来监听当前group中其他consumer的"退出"和"加入";只要此znode path下节点列表变更，
	  都会触发此group下consumer的负载均衡。(比如一个consumer失效,那么其他consumer接管partitions).
	* 在"Broker id 注册"节点下,注册一个watch用来监听broker的存活情况;如果broker列表变更,将会触发所有的groups下的consumer重新balance.


	h) consumer offset
	get /consumers/groupIdxxx/offsets/topicxxx/partitionIdxxx  ->  long (offset)
	用来跟踪每个consumer目前所消费的partition中最大的offset，此znode为持久节点,可以看出offset跟group_id有关,以表明当消费者组中一个
	消费者失效,重新触发balance,其他consumer可以继续消费. 
	[注意] 
	客户端OFFSET 管理：早在0.8.2.2版本，已支持存入消费的 offset 到Topic中，只是那时候默认是将消费的offset存放在
	ZK集群中。0.10.1.1版本以后已默认将消费的offset迁入到了一个名为__consumer_offsets的Topic中，消息的key就是
	group、topic、partition的组合，所有的消费offset都提交写入到__consumer_offsets这个topic中。因为这部分消息
	是非常重要，以至于是不能容忍丢数据的，所以消息的acking级别设置为了-1。
	
	
	
### 
	
	
	
	
	
	
	
	
	
	
	
	
	