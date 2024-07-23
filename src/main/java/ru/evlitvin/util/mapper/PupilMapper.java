package ru.evlitvin.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.evlitvin.dto.PupilDto;
import ru.evlitvin.entity.Pupil;

@Mapper
public interface PupilMapper {

    PupilMapper INSTANCE = Mappers.getMapper(PupilMapper.class);

    PupilDto pupilToPupilDto(Pupil pupil);

    Pupil pupilDtoToPupil(PupilDto pupilDto);
}
