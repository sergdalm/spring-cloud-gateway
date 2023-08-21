package com.sergdalm.firstmicroservice.reactive;

import com.sergdalm.firstmicroservice.domain.ToDo;
import com.sergdalm.firstmicroservice.dto.ToDoCreateDto;
import com.sergdalm.firstmicroservice.repository.ToDoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
@AllArgsConstructor
public class ToDoHandler {

    private ToDoRepository repository;

    public Mono<ServerResponse> getToDo(ServerRequest request) {
        var todoId = request.pathVariable("id");
        var notFound = ServerResponse.notFound().build();
        var toDo = this.repository.findById(todoId);
        return toDo
                .flatMap(t ->
                        ServerResponse
                                .ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(t)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> getToDos(ServerRequest request) {
        Flux<ToDo> toDos = this.repository.findAll();
        return ServerResponse
                .ok()
                .contentType(APPLICATION_JSON)
                .body(toDos, ToDo.class);
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        var toDo = request.bodyToMono(ToDoCreateDto.class)
                .flatMap(createDto -> repository.save(createDto));
        return toDo
                .flatMap(t ->
                        ServerResponse
                                .ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(t)));
    }

}
