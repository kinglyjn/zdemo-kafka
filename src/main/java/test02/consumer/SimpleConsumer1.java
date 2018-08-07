package test02.consumer;

import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * 消息消费者，自动周期性offset
 * @author zhangqingli
 *
 */
public class SimpleConsumer1 {
	
	/**
	 * 获取配置参数
	 * 
	 * [注1]
	 * 客户端OFFSET管理：早在0.8.2.2版本，已支持存入消费的 offset 到Topic中，只是那时候默认是将消费的offset存放在
	 * ZK集群中。0.10.1.1版本以后已默认将消费的offset迁入到了一个名为__consumer_offsets的Topic中，消息的key就是
	 * group、topic、partition的组合，所有的消费offset都提交写入到__consumer_offsets这个topic中。因为这部分消息
	 * 是非常重要，以至于是不能容忍丢数据的，所以消息的acking级别设置为了-1。
	 * 
	 * [注2]
	 * 客户端auto.offset.reset设置：
	 * earliest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费 
	 * latest：当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据 
	 * none：topic各分区都存在已提交的offset时，从offset后开始消费；只要有一个分区不存在已提交的offset，则抛出异常
	 * 
	 * [注3]
	 * request.timeout.ms：
	 * 默认值为305s，它是请求的超时时间，取值需要大于session.timeout.ms。超过此设定时间后，客户端重发消息或消息失败。
	 * session.timeout.ms：
	 * 默认为10s，它是broker接收consumer心跳的超时时间，超过此时间broker没有收到来自客户端的心跳，则broker将从分组
	 * 中移除该消费者并重新将重新平衡该分组的消费。服务关闭和重启之间的时间间隔起码要在session.timeout.ms以上才能接收
	 * 到消息。max.poll.records条数据必须要在session.timeout.ms这个时间内处理完，否则则会出现reblance异常。
	 * max.poll.interval.ms：
	 * 提取消息的最大间隔时间，若在该空闲时间内没有发起pool请求，但heartbeat仍旧在发，就认为该客户端处于livelock状态，
	 * 将该consumer退出consumer group。所以为了不使Consumer自己被退出，Consumer应该不停的发起poll(timeout)操作。
	 * 而这个动作KafkaConsumer Client是不会帮我们做的，这就需要自己在程序中不停的调用poll方法了。
	 * 
	 */
	private static Properties getConfiguration() {
		Properties props = new Properties();
		
		// bootstrap.servers 需要本地设置hosts路由映射
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "hadoop01:9092,hadoop07:9092,hadoop08:9092");
		// group.id
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "group01");
		// enable.auto.commit & auto.commit.interval.ms 设置自动offset，offset的值在0.10.1.1版本以后默认存放在__consumer_offsets主题中
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
		// request.timeout.ms
		props.put("request.timeout.ms", "305000"); 
		// session.timeout.ms
		props.put("session.timeout.ms", "10000");
		// max.poll.interval.ms
		props.put("max.poll.interval.ms", "300000"); 
		// max.poll.records 一次从kafka中poll出来的数据条数，这些数据需要在在session.timeout.ms这个时间内处理完
		props.put("max.poll.records", 500);
		// auto.offset.reset，枚举值为earliest|latest|none
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		// key.deserializer
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		// value.deserializer
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		
		return props;
	}
	
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(getConfiguration());
		consumer.subscribe(Arrays.asList("test0807"));
		
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(200); //获取缓冲区消息的超时时间，如果设置为0则立即获取缓冲区的所有消息
			for (ConsumerRecord<String, String> record : records) {
				System.err.printf("[Consumer] partition=%d, offset=%d, key=%s, value=%s%n", record.partition(), record.offset(), record.key(), record.value());
			}
		}
	}
}
