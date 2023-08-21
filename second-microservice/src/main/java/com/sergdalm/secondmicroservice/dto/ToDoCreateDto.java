package com.sergdalm.secondmicroservice.dto;

import lombok.Value;


@Value
public class ToDoCreateDto {

    String title;
    String content;

}
