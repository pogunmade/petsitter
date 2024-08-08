package com.example.petsitter.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Dog {

    private String name;

    private int age;

    private String breed;

    private String size;
}