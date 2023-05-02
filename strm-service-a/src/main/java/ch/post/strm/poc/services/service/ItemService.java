package ch.post.strm.poc.services.service;

import ch.post.strm.poc.services.model.Item;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
public class ItemService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ItemService.class);

    private final WebClient webClient;
    private ObservationRegistry observationRegistry;

    public ItemService(@Value("${app.item-api-endpoint}") String baseUrl,
                       ObservationRegistry observationRegistry
    ) {
        this.observationRegistry = observationRegistry;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .observationRegistry(observationRegistry)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<Item> findById(String id) {
        LOGGER.debug("Find {}", id);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}").build(id))
                .retrieve()
                .bodyToMono(Item.class);
    }

    public Flux<Item> findAll() {
        LOGGER.debug("Find");
        return webClient.get()
                .uri("/items")
                .retrieve()
                .bodyToFlux(Item.class);
    }

    public Mono<Item> store(Item item) {
        LOGGER.debug("Store {}", item.id());
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}").build(item.id()))
                .body(BodyInserters.fromValue(item.value()))
                .retrieve()
                .bodyToMono(Item.class);
    }

}
