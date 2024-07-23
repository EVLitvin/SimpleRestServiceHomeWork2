package ru.evlitvin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.evlitvin.dao.SchoolDao;
import ru.evlitvin.dto.SchoolDto;
import ru.evlitvin.entity.School;
import ru.evlitvin.util.mapper.SchoolMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DisplayName("Tests for SchoolService class")
class SchoolServiceTest {

    private SchoolDao schoolDao;
    private SchoolService schoolService;
    private School schoolOne;

    @BeforeEach
    void setUp() {
        schoolDao= Mockito.mock(SchoolDao.class);
        schoolService = new SchoolService(schoolDao);

        schoolOne = new School();
        schoolOne.setId(1L);
        schoolOne.setName("School One name");
        schoolOne.setAddress("School One address");

    }

    @Test
    @DisplayName("SchoolService save School instance test")
    void save() throws SQLException {
        schoolService.save(SchoolMapper.INSTANCE.schoolToSchoolDto(schoolOne));

        verify(schoolDao, Mockito.times(1)).insert(any(School.class));
    }

    @Test
    @DisplayName("SchoolService get all schools test")
    void getAll() throws SQLException {
        Mockito.when(schoolDao.findAll()).thenReturn(Collections.singletonList(schoolOne));

        List<SchoolDto> teachers = schoolService.getAll();
        assertEquals(1, teachers.size());
        assertEquals("School One name", teachers.get(0).getName());
    }

    @Test
    @DisplayName("SchoolService get school by ID test")
    void getById() throws SQLException {
        Mockito.when(schoolDao.findById(1L)).thenReturn(schoolOne);

        SchoolDto schoolDto = schoolService.getById(1L);
        assertEquals("School One name", schoolDto.getName());
    }

    @Test
    @DisplayName("SchoolService update school data test")
    void update() throws SQLException {
        schoolService.update(SchoolMapper.INSTANCE.schoolToSchoolDto(schoolOne));

        verify(schoolDao, Mockito.times(1)).update(any(School.class));
    }

    @Test
    @DisplayName("SchoolService delete school by ID test")
    void delete() throws SQLException {
        schoolService.delete(1L);

        verify(schoolDao, Mockito.times(1)).delete(1L);
    }
}