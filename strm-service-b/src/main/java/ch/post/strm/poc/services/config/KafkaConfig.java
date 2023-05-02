package ch.post.strm.poc.services.config;

import ch.post.strm.poc.services.model.Item;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean
    public ConsumerFactory<String, Item> consumerFactory(MeterRegistry meterRegistry) {
        DefaultKafkaConsumerFactory<String, Item> factory = new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry));
        factory.setKeyDeserializer(new StringDeserializer());
        factory.setValueDeserializer(new JsonDeserializer<>(Item.class));
        return factory;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Item>> kafkaListenerContainerFactory(ConsumerFactory<String, Item> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Item> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ProducerFactory<String, Item> producerFactory(MeterRegistry meterRegistry) {
        DefaultKafkaProducerFactory<String, Item> factory = new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
        factory.addListener(new MicrometerProducerListener<>(meterRegistry));
        factory.setKeySerializer(new StringSerializer());
        factory.setValueSerializer(new JsonSerializer<Item>().noTypeInfo());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Item> kafkaTemplate(ProducerFactory<String, Item> producerFactory) {
        KafkaTemplate<String, Item> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setObservationEnabled(true);
        return kafkaTemplate;
    }

}
