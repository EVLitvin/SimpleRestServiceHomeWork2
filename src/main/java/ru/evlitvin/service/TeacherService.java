package ru.evlitvin.service;

import ru.evlitvin.dao.TeacherDao;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.entity.Teacher;
import ru.evlitvin.service.interfaces.CrudServiceInterface;
import ru.evlitvin.util.mapper.TeacherMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherService implements CrudServiceInterface<TeacherDto> {

    private final TeacherDao teacherDao;

    public TeacherService(TeacherDao teacherDao) {
        this.teacherDao = teacherDao;
    }

    @Override
    public void save(TeacherDto teacherDto) throws SQLException, ClassNotFoundException {
        Teacher teacher = TeacherMapper.INSTANCE.teacherDtoToTeacher(teacherDto);
        teacherDao.insert(teacher);
    }

    @Override
    public List<TeacherDto> getAll() throws SQLException, ClassNotFoundException {
        List<Teacher> teachers = teacherDao.findAll();
        return teachers.stream().map(TeacherMapper.INSTANCE::teacherToTeacherDto).collect(Collectors.toList());
    }

    @Override
    public TeacherDto getById(Long id) throws SQLException, ClassNotFoundException {
        Teacher teacher = teacherDao.findById(id);
        return TeacherMapper.INSTANCE.teacherToTeacherDto(teacher);
    }

    @Override
    public void update(TeacherDto teacherDto) throws SQLException, ClassNotFoundException {
        Teacher teacher = TeacherMapper.INSTANCE.teacherDtoToTeacher(teacherDto);
        teacherDao.update(teacher);
    }

    @Override
    public void delete(Long id) throws SQLException, ClassNotFoundException {
        teacherDao.delete(id);
    }
}
