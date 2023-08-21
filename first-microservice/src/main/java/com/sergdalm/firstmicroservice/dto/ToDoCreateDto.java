package com.sergdalm.firstmicroservice.dto;

import lombok.Value;


@Value
public class ToDoCreateDto {

    String description;
    boolean completed;
}
