package com.example.petsitter.common;

import lombok.Getter;

import java.util.Collection;

public class CollectionDto<E> {

    @Getter
    Collection<E> items;

    public CollectionDto(Collection<E> items) {
        this.items = items;
    }
}
