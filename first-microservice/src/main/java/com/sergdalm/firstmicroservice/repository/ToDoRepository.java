package com.sergdalm.firstmicroservice.repository;

import com.sergdalm.firstmicroservice.domain.ToDo;
import com.sergdalm.firstmicroservice.dto.ToDoCreateDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class ToDoRepository {

    private Flux<ToDo> toDoFlux = Flux.fromIterable(
            Arrays.asList(
                    new ToDo("Do homework"),
                    new ToDo("Workout in the morning"),
                    new ToDo("Clean the room", true)
            )
    );

    public Mono<ToDo> findById(String id) {
        return Mono.from(toDoFlux.filter(todo -> todo.getId().equals(id)));
    }

    public Flux<ToDo> findAll() {
        return toDoFlux;
    }

    public Mono<ToDo> save(ToDoCreateDto toDo) {
        var newToDo = new ToDo(toDo.getDescription(), toDo.isCompleted());
        toDoFlux = Flux.concat(toDoFlux, Flux.just(newToDo));
        return Mono.just(newToDo);
    }

}
