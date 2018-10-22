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
public class ProducerInterceptor02 implements ProducerInterceptor<String,String> {
    private long successCount = 0;
    private long falureCount = 0;

    /**
     * Counter Interceptor
     *
     */
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
       return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception==null) {
            successCount++;
        } else {
            falureCount++;
        }
    }
    @Override
    public void close() {
        System.out.printf("successCount=%d, falureCount=%d%n", successCount, falureCount);
    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
