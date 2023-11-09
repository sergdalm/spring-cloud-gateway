package com.sergdalm.secondmicroservice.controller;

import com.sergdalm.secondmicroservice.domain.Memo;
import com.sergdalm.secondmicroservice.dto.ToDoCreateDto;
import com.sergdalm.secondmicroservice.repository.ToDoRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/second")
public class ToDoController {

    private ToDoRepository repository;

    @GetMapping("{id}")
    public Mono<Memo> getToDo(@PathVariable String id) {
        return this.repository.findById(id);
    }


    @GetMapping
    public Flux<Memo> getToDos() {
        return this.repository.findAll();
    }

    @GetMapping("/string")
    public Mono<String> getHello() {
        return Mono.just("Hello!");
    }

    @PostMapping
    public Mono<Memo> save(@RequestBody ToDoCreateDto todo) {
        return repository.save(todo);
    }

}
