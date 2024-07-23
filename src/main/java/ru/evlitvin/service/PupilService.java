package ru.evlitvin.service;

import ru.evlitvin.dao.PupilDAO;
import ru.evlitvin.dto.PupilDto;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.entity.Pupil;
import ru.evlitvin.entity.Teacher;
import ru.evlitvin.service.interfaces.CrudServiceInterface;
import ru.evlitvin.service.interfaces.PupilServiceInterface;
import ru.evlitvin.util.mapper.PupilMapper;
import ru.evlitvin.util.mapper.TeacherMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PupilService implements CrudServiceInterface<PupilDto>, PupilServiceInterface {

    private final PupilDAO pupilDAO;

    public PupilService(PupilDAO pupilDAO) {
        this.pupilDAO = pupilDAO;
    }

    @Override
    public void save(PupilDto pupilDto) throws SQLException, ClassNotFoundException {
        Pupil pupil = PupilMapper.INSTANCE.pupilDtoToPupil(pupilDto);
        pupilDAO.insert(pupil);
    }

    @Override
    public List<PupilDto> getAll() throws SQLException, ClassNotFoundException {
        List<Pupil> pupils = pupilDAO.findAll();
        return pupils.stream()
                .map(PupilMapper.INSTANCE::pupilToPupilDto).collect(Collectors.toList());
    }

    @Override
    public PupilDto getById(Long id) throws SQLException, ClassNotFoundException {
        Pupil pupil = pupilDAO.findById(id);
        return PupilMapper.INSTANCE.pupilToPupilDto(pupil);
    }

    @Override
    public void update(PupilDto pupilDto) throws SQLException, ClassNotFoundException {
        Pupil pupil = PupilMapper.INSTANCE.pupilDtoToPupil(pupilDto);
        pupilDAO.update(pupil);
    }

    @Override
    public void delete(Long id) throws SQLException, ClassNotFoundException {
        pupilDAO.delete(id);
    }

    @Override
    public void addPupilToTeacher(Long pupilId, Long teacherId) throws SQLException {
        pupilDAO.addPupilToTeacherList(pupilId, teacherId);
    }

    @Override
    public List<PupilDto> getAllPupilsByTeacher(Long teacherId) throws SQLException {
        List<Pupil> pupils = pupilDAO.findPupilsByTeacherId(teacherId);
        return pupils.stream().map(PupilMapper.INSTANCE::pupilToPupilDto).collect(Collectors.toList());
    }

    @Override
    public void removePupilFromTeacher(Long pupilId, Long teacherId) throws SQLException {
        pupilDAO.deletePupilFromTeacherList(pupilId, teacherId);
    }

    @Override
    public List<TeacherDto> getAllTeachersByPupil(Long pupilId) throws SQLException {
        List<Teacher> teachers = pupilDAO.findTeachersByPupilId(pupilId);
        return teachers.stream()
                .map(TeacherMapper.INSTANCE::teacherToTeacherDto).collect(Collectors.toList());
    }
}
