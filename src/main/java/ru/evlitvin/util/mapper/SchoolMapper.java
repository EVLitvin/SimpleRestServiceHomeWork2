package ru.evlitvin.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.evlitvin.dto.SchoolDto;
import ru.evlitvin.entity.School;

@Mapper
public interface SchoolMapper {

    SchoolMapper INSTANCE = Mappers.getMapper(SchoolMapper.class);

    @Mapping(source = "teachers", target = "teachers")
    SchoolDto schoolToSchoolDto(School school);

    @Mapping(source = "teachers", target = "teachers")
    School schoolDtoToSchool(SchoolDto schoolDto);
}
