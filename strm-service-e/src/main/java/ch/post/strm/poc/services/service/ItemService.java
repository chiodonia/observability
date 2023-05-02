package ch.post.strm.poc.services.service;

import ch.post.strm.poc.services.config.ProducerRecordPropagatorSetter;
import ch.post.strm.poc.services.model.Item;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.SenderContext;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);
    private final ObservationRegistry observationRegistry;
    private final Producer<String, Item> producer;
    private final KafkaClientMetrics kafkaClientMetrics;

    public ItemService(KafkaProperties kafkaProperties, ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        this.observationRegistry = observationRegistry;
        this.producer = new KafkaProducer<>(
                kafkaProperties.buildProducerProperties(), new StringSerializer(), new JsonSerializer<Item>().noTypeInfo());
        //To get the metrics published!
        this.kafkaClientMetrics = new KafkaClientMetrics(producer);
        this.kafkaClientMetrics.bindTo(meterRegistry);
    }

    @Scheduled(fixedRate = 5000)
    public void generate() {
        try {
            Item item = new Item(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            SenderContext<ProducerRecord<String, Item>> context = new SenderContext(new ProducerRecordPropagatorSetter());
            ProducerRecord<String, Item> record = new ProducerRecord<>("t3", item.id(), item);
            context.setCarrier(record);
            Observation.createNotStarted("generate", () -> context, observationRegistry).observe(() -> {
                RecordMetadata metadata = null;
                try {
                    metadata = producer.send(record).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                LOGGER.debug("Record sent: {}", metadata);
            });
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
    }

    @PreDestroy
    public void close() {
        if (kafkaClientMetrics != null) {
            kafkaClientMetrics.close();
        }
        if (producer != null) {
            producer.close();
        }
    }

}
