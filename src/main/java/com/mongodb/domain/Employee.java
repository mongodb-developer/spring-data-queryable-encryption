package com.mongodb.domain;


import org.springframework.data.annotation.Id;

public record Employee(
        @Id String id,
        String name,
        double salary,
        int age
) {
}


