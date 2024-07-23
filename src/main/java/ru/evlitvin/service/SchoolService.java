package ru.evlitvin.service;

import ru.evlitvin.dao.SchoolDao;
import ru.evlitvin.dto.SchoolDto;
import ru.evlitvin.entity.School;
import ru.evlitvin.service.interfaces.CrudServiceInterface;
import ru.evlitvin.util.mapper.SchoolMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SchoolService implements CrudServiceInterface<SchoolDto> {

    private final SchoolDao schoolDao;

    public SchoolService(SchoolDao schoolDao) {
        this.schoolDao = schoolDao;
    }

    @Override
    public void save(SchoolDto schoolDto) throws SQLException {
        School school = SchoolMapper.INSTANCE.schoolDtoToSchool(schoolDto);
        schoolDao.insert(school);
    }

    @Override
    public List<SchoolDto> getAll() throws SQLException {
        List<School> schools = schoolDao.findAll();
        return schools.stream().map(SchoolMapper.INSTANCE::schoolToSchoolDto).collect(Collectors.toList());
    }

    @Override
    public SchoolDto getById(Long id) throws SQLException {
        School school = schoolDao.findById(id);
        return SchoolMapper.INSTANCE.schoolToSchoolDto(school);
    }

    @Override
    public void update(SchoolDto schoolDto) throws SQLException {
        School school = SchoolMapper.INSTANCE.schoolDtoToSchool(schoolDto);
        schoolDao.update(school);
    }

    @Override
    public void delete(Long id) throws SQLException {
        schoolDao.delete(id);
    }
}
