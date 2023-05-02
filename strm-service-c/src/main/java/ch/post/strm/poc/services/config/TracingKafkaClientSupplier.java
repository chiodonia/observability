package ch.post.strm.poc.services.config;

import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.streams.KafkaClientSupplier;

import java.util.Map;

public class TracingKafkaClientSupplier implements KafkaClientSupplier {

    private final ObservationRegistry observationRegistry;

    public TracingKafkaClientSupplier(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Admin getAdmin(Map<String, Object> config) {
        //Not traced!
        return Admin.create(config);
    }

    @Override
    public Producer<byte[], byte[]> getProducer(Map<String, Object> config) {
        config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        config.put(TracingProducerInterceptor.OBSERVATION_REGISTRY_CONFIG, observationRegistry);
        return new KafkaProducer<>(config, new ByteArraySerializer(), new ByteArraySerializer());
    }

    @Override
    public Consumer<byte[], byte[]> getConsumer(Map<String, Object> config) {
        return new KafkaConsumer<>(config, new ByteArrayDeserializer(), new ByteArrayDeserializer());
    }

    @Override
    public Consumer<byte[], byte[]> getRestoreConsumer(Map<String, Object> config) {
        return getConsumer(config);
    }

    @Override
    public Consumer<byte[], byte[]> getGlobalConsumer(Map<String, Object> config) {
        return getConsumer(config);
    }
}
