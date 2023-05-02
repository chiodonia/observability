package ch.post.strm.poc.services.service;

import ch.post.strm.poc.services.model.Item;
import ch.post.strm.poc.services.repository.ItemRepository;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ItemService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    public ItemRepository repository;
    @Autowired
    private KafkaTemplate<String, Item> kafkaTemplate;
    @Autowired
    private ObservationRegistry observationRegistry;

    @KafkaListener(topics = "t3", containerFactory = "kafkaListenerContainerFactory")
    public void process(Item item) {
        store(item);
    }

    public Flux<Item> findAll() {
        LOGGER.debug("Find");
        return repository.findAll();
    }

    public Mono<Item> findById(String id) {
        LOGGER.debug("Find {}", id);
        return repository.findById(id);
    }

    public void store(Item item) {
        LOGGER.debug("Store {}", item.id());
        kafkaTemplate.send("t1", item.id(), item);
        repository.store(item);
    }
}
