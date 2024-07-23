package ru.evlitvin.util.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.entity.School;
import ru.evlitvin.entity.Teacher;

@Mapper
public interface TeacherMapper {

    TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

    @Mapping(source = "school.id", target = "schoolId")
    TeacherDto teacherToTeacherDto(Teacher teacher);

    @Mapping(source = "schoolId", target = "school.id")
    Teacher teacherDtoToTeacher(TeacherDto teacherDto);

    @AfterMapping
    default void setSchool(TeacherDto teacherDto, @MappingTarget Teacher teacher) {
        if (teacherDto.getSchoolId() != null) {
            School school = new School();
            school.setId(teacherDto.getSchoolId());
            teacher.setSchool(school);
        }
    }
}
