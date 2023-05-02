package ch.post.strm.poc.services.api;

import ch.post.strm.poc.services.model.Item;
import ch.post.strm.poc.services.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class ItemApi {
    @Autowired
    public ItemService service;

    @Bean
    RouterFunction<ServerResponse> routes() {
        return route(GET("/items"), req -> ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Item.class))
                .andRoute(GET("/items/{id}"), req -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.findById(req.pathVariable("id")), Item.class))
                .andRoute(POST("/items/{id}"),
                        req -> req.body(toMono(String.class))
                                .doOnNext(value -> service.store(new Item(req.pathVariable("id"), value)))
                                .then(ok().build()));
    }

}
