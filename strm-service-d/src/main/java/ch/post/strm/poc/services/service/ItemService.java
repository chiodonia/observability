package ch.post.strm.poc.services.service;

import ch.post.strm.poc.services.config.ConsumerRecordPropagatorGetter;
import ch.post.strm.poc.services.model.Item;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.ReceiverContext;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);

    private final ObservationRegistry observationRegistry;
    private final Consumer<String, Item> consumer;
    private final KafkaClientMetrics kafkaClientMetrics;

    public ItemService(KafkaProperties kafkaProperties, ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        this.observationRegistry = observationRegistry;
        this.consumer = new KafkaConsumer<>(
                kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new JsonDeserializer<>(Item.class));
        //To get the metrics published!
        this.kafkaClientMetrics = new KafkaClientMetrics(consumer);
        this.kafkaClientMetrics.bindTo(meterRegistry);
        this.consumer.subscribe(Collections.singleton("t2"));
    }

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        try {
            ConsumerRecords<String, Item> records = consumer.poll(Duration.ofMillis(500));
            LOGGER.debug("Polled {} records", records.count());
            records.forEach(record -> {
                ReceiverContext<ConsumerRecord<String, Item>> context = new ReceiverContext(new ConsumerRecordPropagatorGetter());
                context.setCarrier(record);
                Observation.createNotStarted("consume", ()->context, observationRegistry).observe(() ->
                        LOGGER.debug("Consumed {}-{}@{}", record.topic(), record.partition(), record.offset())
                );
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
        if (consumer != null) {
            consumer.close();
        }
    }
}
