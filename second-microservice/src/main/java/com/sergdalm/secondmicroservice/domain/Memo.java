package com.sergdalm.secondmicroservice.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Memo {

    private String id;
    private String title;
    private String content;
    private LocalDateTime created;
    private LocalDateTime modified;

    public Memo() {
        this.id = UUID.randomUUID().toString();
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
    }

    public Memo(String title, String content) {
        this();
        this.title = title;
        this.content = content;
    }

}
