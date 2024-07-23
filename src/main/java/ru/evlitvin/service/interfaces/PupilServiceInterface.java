package ru.evlitvin.service.interfaces;

import ru.evlitvin.dto.PupilDto;
import ru.evlitvin.dto.TeacherDto;

import java.sql.SQLException;
import java.util.List;

public interface PupilServiceInterface {

    void addPupilToTeacher(Long pupilId, Long teacherId) throws SQLException;

    List<PupilDto> getAllPupilsByTeacher(Long teacherId) throws SQLException;

    void removePupilFromTeacher(Long pupilId, Long teacherId) throws SQLException;

    List<TeacherDto> getAllTeachersByPupil(Long pupilId) throws SQLException;

}
