package ch.post.strm.poc.services.config;

import io.micrometer.observation.transport.Propagator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.nio.charset.StandardCharsets;

public class ConsumerRecordPropagatorGetter implements Propagator.Getter<ConsumerRecord<?, ?>> {
    @Override
    public String get(ConsumerRecord<?, ?> carrier, String key) {
        Header header = carrier.headers().lastHeader(key);
        if (header == null) {
            return null;
        } else {
            byte[] value = header.value();
            return value == null ? null : new String(value, StandardCharsets.UTF_8);
        }
    }
}
