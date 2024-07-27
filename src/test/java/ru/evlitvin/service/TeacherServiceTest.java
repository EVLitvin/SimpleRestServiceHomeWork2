package ru.evlitvin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.evlitvin.dao.TeacherDao;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.entity.Teacher;
import ru.evlitvin.util.mapper.TeacherMapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

@DisplayName("Tests for TeacherService class")
class TeacherServiceTest {

    @Mock
    private TeacherDao teacherDao;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherService teacherService;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("TeacherService save Teacher instance test")
    void save() throws SQLException, ClassNotFoundException {
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setFirstName("Ivan");
        teacherDto.setLastName("Ivanov");

        Teacher teacher = new Teacher();
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivanov");

        when(teacherMapper.teacherDtoToTeacher(teacherDto)).thenReturn(teacher);

        teacherService.save(teacherDto);

        verify(teacherDao, times(1)).insert(teacher);
    }

    @Test
    @DisplayName("TeacherService get Teacher by ID test")
    void getById() throws SQLException, ClassNotFoundException {
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setFirstName("Ivan");
        teacherDto.setLastName("Ivanov");

        Teacher teacher = new Teacher();
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivanov");

        when(teacherDao.findById(1L)).thenReturn(teacher);
        when(teacherMapper.teacherToTeacherDto(teacher)).thenReturn(teacherDto);

        TeacherDto teacherDtoFound = teacherService.getById(1L);

        assertNotNull(teacherDtoFound);
        assertEquals("Ivan", teacherDtoFound.getFirstName());
        assertEquals("Ivanov", teacherDtoFound.getLastName());
    }

    @Test
    @DisplayName("TeacherService get all Teachers test")
    void getAll() throws SQLException, ClassNotFoundException {
        Teacher teacherOne = new Teacher();
        teacherOne.setId(1L);
        teacherOne.setFirstName("Ivan");
        teacherOne.setLastName("Ivanov");

        Teacher teacherTwo = new Teacher();
        teacherTwo.setId(2L);
        teacherTwo.setFirstName("Petr");
        teacherTwo.setLastName("Kuznecov");

        List<Teacher> teachers = Arrays.asList(teacherOne, teacherTwo);

        TeacherDto teacherDtoOne = new TeacherDto();
        teacherDtoOne.setId(1L);
        teacherDtoOne.setFirstName("Ivan");
        teacherDtoOne.setLastName("Ivanov");

        TeacherDto teacherDtoTwo = new TeacherDto();
        teacherDtoTwo.setId(2L);
        teacherDtoTwo.setFirstName("Petr");
        teacherDtoTwo.setLastName("Kuznecov");

        when(teacherDao.findAll()).thenReturn(teachers);
        when(teacherMapper.teacherToTeacherDto(teacherOne)).thenReturn(teacherDtoOne);
        when(teacherMapper.teacherToTeacherDto(teacherTwo)).thenReturn(teacherDtoTwo);

        List<TeacherDto> teacherDtos = teacherService.getAll();

        assertNotNull(teacherDtos);
        assertEquals(2, teacherDtos.size());
        assertEquals("Ivan", teacherDtos.get(0).getFirstName());
        assertEquals("Kuznecov", teacherDtos.get(1).getLastName());
    }

    @Test
    @DisplayName("TeacherService update teacher data test")
    void update() throws SQLException, ClassNotFoundException {
        TeacherDto teacherDto = new TeacherDto();
        teacherDto.setId(1L);
        teacherDto.setFirstName("Ivan");
        teacherDto.setLastName("Ivanov");

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("Petr");
        teacher.setLastName("Kuznecov");

        when(teacherMapper.teacherDtoToTeacher(teacherDto)).thenReturn(teacher);

        teacherService.update(teacherDto);

        verify(teacherDao, times(1)).update(any(Teacher.class));
    }

    @Test
    @DisplayName("TeacherService delete teacher by ID test")
    void delete() throws SQLException, ClassNotFoundException {
        teacherService.delete(1L);

        verify(teacherDao, times(1)).delete(1L);
    }

}