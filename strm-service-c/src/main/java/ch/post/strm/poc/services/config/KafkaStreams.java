package ch.post.strm.poc.services.config;

import ch.post.strm.poc.services.model.Item;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.ReceiverContext;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafkaStreams
public class KafkaStreams {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreams.class);
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Bean
    public StreamsBuilderFactoryBeanConfigurer configurer() {
        return fb -> {
            fb.addListener(new KafkaStreamsMicrometerListener(meterRegistry));
            fb.setClientSupplier(new TracingKafkaClientSupplier(observationRegistry));
            fb.setStateListener((newState, oldState) -> {
                LOGGER.debug("State transition from {} to {}", oldState, newState);
            });
        };
    }

    @Bean
    public KStream<String, Item> processor(StreamsBuilder builder) {
        KStream<String, Item> stream = builder.stream("t1", Consumed.with(
                        Serdes.String(),
                        Serdes.serdeFrom(
                                new JsonSerializer<Item>().noTypeInfo(),
                                new JsonDeserializer(Item.class)
                        )
                )
        );

        stream
                .processValues(new FixedKeyProcessorSupplier<String, Item, Item>() {
                    @Override
                    public FixedKeyProcessor<String, Item, Item> get() {
                        return new ContextualFixedKeyProcessor<String, Item, Item>() {

                            @Override
                            public void process(FixedKeyRecord<String, Item> record) {
                                ReceiverContext<FixedKeyRecord<String, Item>> context = new ReceiverContext(new FixedKeyRecordPropagatorGetter());
                                context.setCarrier(record);
                                context().recordMetadata().ifPresent(recordMetadata -> context.setRemoteServiceName(recordMetadata.topic()));
                                Observation.createNotStarted("consume", () -> context, observationRegistry).observe(() -> {
                                    LOGGER.debug("Processing {}", record.key());
                                    context().forward(record);
                                    LOGGER.debug("Processed {}", record.key());
                                });
                            }
                        };
                    }
                })
                .mapValues((key, item) -> new Item(item.id(), item.value().toUpperCase()))
                .to("t2",
                        Produced.with(
                                Serdes.String(),
                                Serdes.serdeFrom(
                                        new JsonSerializer<Item>().noTypeInfo(),
                                        new JsonDeserializer(Item.class)
                                )
                        )
                );

        return stream;
    }

}
