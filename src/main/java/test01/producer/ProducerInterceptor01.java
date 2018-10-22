package test01.producer;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.Map;

/**
 * @Author Kingly
 * @Date 2018/10/22
 * @Description
 */
public class ProducerInterceptor01 implements ProducerInterceptor<String,String> {

    /**
     * Timer Interceptor
     *
     */
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
        return new ProducerRecord<>(
                record.topic(),
                record.partition(),
                record.key(),
                System.currentTimeMillis() + "," + record.value());
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
