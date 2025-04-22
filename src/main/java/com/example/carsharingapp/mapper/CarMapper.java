package com.example.carsharingapp.mapper;

import com.example.carsharingapp.config.MapperConfig;
import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CreateCarRequestDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarDto toDto(Car car);

    Car toModel(CreateCarRequestDto requestDto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "dailyFee", source = "dailyFee"),
            @Mapping(target = "inventory", source = "inventory")
    })
    void updateCarFromDto(UpdateCarRequestDto requestDto, @MappingTarget Car car);
}
