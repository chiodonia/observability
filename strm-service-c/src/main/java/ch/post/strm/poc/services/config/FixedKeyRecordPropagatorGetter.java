package ch.post.strm.poc.services.config;

import io.micrometer.observation.transport.Propagator;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;

import java.nio.charset.StandardCharsets;

public class FixedKeyRecordPropagatorGetter implements Propagator.Getter<FixedKeyRecord<?, ?>> {
    @Override
    public String get(FixedKeyRecord<?, ?> carrier, String key) {
        Header header = carrier.headers().lastHeader(key);
        if (header == null) {
            return null;
        } else {
            byte[] value = header.value();
            return value == null ? null : new String(value, StandardCharsets.UTF_8);
        }
    }
}
