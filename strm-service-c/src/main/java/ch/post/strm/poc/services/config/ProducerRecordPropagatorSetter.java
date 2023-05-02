package ch.post.strm.poc.services.config;

import io.micrometer.observation.transport.Propagator;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

public class ProducerRecordPropagatorSetter implements Propagator.Setter<ProducerRecord<?, ?>> {
    @Override
    public void set(ProducerRecord<?, ?> record, String key, String value) {
        assert record != null;
        record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
    }
}
