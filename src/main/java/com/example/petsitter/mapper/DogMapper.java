package com.example.petsitter.mapper;

import com.example.petsitter.dto.DogDTO;
import com.example.petsitter.model.Dog;
import org.mapstruct.Mapper;

@Mapper
public interface DogMapper {

    Dog dogDTOToDog(DogDTO dogDTO);

    DogDTO dogToDogTO(Dog dog);
}
