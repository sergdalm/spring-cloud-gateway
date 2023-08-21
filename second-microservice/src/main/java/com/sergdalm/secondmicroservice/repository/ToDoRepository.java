package com.sergdalm.secondmicroservice.repository;

import com.sergdalm.secondmicroservice.domain.Memo;
import com.sergdalm.secondmicroservice.dto.ToDoCreateDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class ToDoRepository {

    private Flux<Memo> toDoFlux = Flux.fromIterable(
            Arrays.asList(
                    new Memo("First", "First memo"),
                    new Memo("Second", "Second memo")
            )
    );

    public Mono<Memo> findById(String id) {
        return Mono.from(toDoFlux.filter(todo -> todo.getId().equals(id)));
    }

    public Flux<Memo> findAll() {
        return toDoFlux;
    }

    public Mono<Memo> save(ToDoCreateDto toDo) {
        var newToDo = new Memo(toDo.getTitle(), toDo.getContent());
        toDoFlux = Flux.concat(toDoFlux, Flux.just(newToDo));
        return Mono.just(newToDo);
    }

}
