package ru.evlitvin.dao.interfaces;

import ru.evlitvin.entity.Pupil;
import ru.evlitvin.entity.Teacher;

import java.sql.SQLException;
import java.util.List;

public interface PupilDAOInterface {

    void addPupilToTeacherList(Long pupilId, Long teacherId) throws SQLException;

    List<Pupil> findPupilsByTeacherId(Long teacherId) throws SQLException;

    void deletePupilFromTeacherList(Long pupilId, Long teacherId) throws SQLException;

    List<Teacher> findTeachersByPupilId(Long pupilId) throws SQLException;

}
