package ch.post.strm.poc.services.repository;

import ch.post.strm.poc.services.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ItemRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRepository.class);
    public final Map<String, Item> items = new ConcurrentHashMap<>();

    public Mono<Item> findById(String id) {
        LOGGER.debug("Find {}", id);
        return Mono.justOrEmpty(items.get(id));
    }

    public Flux<Item> findAll() {
        LOGGER.debug("Find");
        return Flux.fromIterable(items.values());
    }

    public void store(Item item) {
        LOGGER.debug("Store {}", item.id());
        items.put(item.id(), item);
    }
}
