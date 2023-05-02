package ch.post.strm.poc.services.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.SenderContext;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TracingProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TracingProducerInterceptor.class);

    public static final String OBSERVATION_REGISTRY_CONFIG = "observation.registry";
    private ObservationRegistry observationRegistry;

    @Override
    public void configure(Map<String, ?> configs) {
        this.observationRegistry = (ObservationRegistry) configs.get(OBSERVATION_REGISTRY_CONFIG);
    }

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        SenderContext<ProducerRecord<K, V>> context = new SenderContext(new ProducerRecordPropagatorSetter());
        context.setCarrier(record);
        Observation.createNotStarted("send", () -> context, observationRegistry).observe(() -> {
            LOGGER.debug("Send {}-{}", record.topic(), record.partition() != null ? record.partition() : "?");
        });
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }
}
