package ru.evlitvin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.evlitvin.dao.PupilDAO;
import ru.evlitvin.dto.PupilDto;
import ru.evlitvin.dto.TeacherDto;
import ru.evlitvin.entity.Pupil;
import ru.evlitvin.entity.Teacher;
import ru.evlitvin.util.mapper.PupilMapper;
import ru.evlitvin.util.mapper.TeacherMapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests for PupilService class")
class PupilServiceTest {

    @Mock
    private PupilDAO pupilDAO;

    @Mock
    private PupilMapper pupilMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private PupilService pupilService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("PupilService save Pupil instance test")
    void testSaveTag() throws SQLException, ClassNotFoundException {
        PupilDto pupilDto = new PupilDto();
        pupilDto.setFirstName("Vladimir");
        pupilDto.setLastName("Markov");

        Pupil pupil = new Pupil();
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markov");

        when(pupilMapper.pupilDtoToPupil(pupilDto)).thenReturn(pupil);

        pupilService.save(pupilDto);

        verify(pupilDAO, times(1)).insert(pupil);
    }

    @Test
    @DisplayName("PupilService get Pupil by ID test")
    void getById() throws SQLException, ClassNotFoundException {
        Pupil pupil = new Pupil();
        pupil.setId(1L);
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markov");

        PupilDto pupilDto = new PupilDto();
        pupilDto.setId(1L);
        pupilDto.setFirstName("Vladimir");
        pupilDto.setLastName("Markov");

        when(pupilDAO.findById(1L)).thenReturn(pupil);
        when(pupilMapper.pupilToPupilDto(pupil)).thenReturn(pupilDto);

        PupilDto savedPupil = pupilService.getById(1L);

        assertNotNull(savedPupil);
        assertEquals("Vladimir", savedPupil.getFirstName());
        assertEquals("Markov", savedPupil.getLastName());
    }

    @Test
    @DisplayName("PupilService get all Pupils test")
    void getAll() throws SQLException, ClassNotFoundException {
        Pupil pupilOne = new Pupil();
        pupilOne.setId(1L);
        pupilOne.setFirstName("Vladimir");
        pupilOne.setLastName("Markov");

        Pupil pupilTwo = new Pupil();
        pupilTwo.setId(2L);
        pupilTwo.setFirstName("Masha");
        pupilTwo.setLastName("Sidorova");

        List<Pupil> pupils = Arrays.asList(pupilOne, pupilTwo);

        PupilDto pupilDtoOne = new PupilDto();
        pupilDtoOne.setId(1L);
        pupilDtoOne.setFirstName("Vladimir");
        pupilDtoOne.setLastName("Markov");

        PupilDto pupilDtoTwo = new PupilDto();
        pupilDtoTwo.setId(2L);
        pupilDtoTwo.setFirstName("Masha");
        pupilDtoTwo.setLastName("Sidorova");

        when(pupilDAO.findAll()).thenReturn(pupils);
        when(pupilMapper.pupilToPupilDto(pupilOne)).thenReturn(pupilDtoOne);
        when(pupilMapper.pupilToPupilDto(pupilTwo)).thenReturn(pupilDtoTwo);

        List<PupilDto> pupilDtos = pupilService.getAll();

        assertNotNull(pupilDtos);
        assertEquals(2, pupilDtos.size());
        assertEquals("Vladimir", pupilDtos.get(0).getFirstName());
        assertEquals("Sidorova", pupilDtos.get(1).getLastName());
    }

    @Test
    @DisplayName("PupilService update Pupil data test")
    void update() throws SQLException, ClassNotFoundException {
        PupilDto pupilDto = new PupilDto();
        pupilDto.setId(1L);
        pupilDto.setFirstName("Vladimir");
        pupilDto.setLastName("Markov");

        Pupil pupil = new Pupil();
        pupil.setId(1L);
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markov");

        when(pupilMapper.pupilDtoToPupil(pupilDto)).thenReturn(pupil);

        pupilService.update(pupilDto);

        verify(pupilDAO, times(1)).update(pupil);

    }

    @Test
    @DisplayName("PupilService delete Pupil by ID test")
    void delete() throws SQLException, ClassNotFoundException {
        pupilService.delete(1L);

        verify(pupilDAO, times(1)).delete(1L);
    }

    @Test
    @DisplayName("PupilService add Pupil to Teacher test")
    void addPupilToTeacher() throws SQLException {
        pupilService.addPupilToTeacher(1L, 1L);

        verify(pupilDAO, times(1)).addPupilToTeacherList(1L, 1L);
    }

    @Test
    @DisplayName("PupilService get Pupil list by Teacher ID test")
    void getAllPupilsByTeacher() throws SQLException {
        Long teacherId = 1L;

        Pupil pupilOne = new Pupil();
        pupilOne.setId(1L);
        pupilOne.setFirstName("Vladimir");
        pupilOne.setLastName("Markov");

        Pupil pupilTwo = new Pupil();
        pupilTwo.setId(2L);
        pupilTwo.setFirstName("Masha");
        pupilTwo.setLastName("Sidorova");

        List<Pupil> pupils = Arrays.asList(pupilOne, pupilTwo);

        PupilDto pupilDtoOne = new PupilDto();
        pupilDtoOne.setId(1L);
        pupilDtoOne.setFirstName("Vladimir");
        pupilDtoOne.setLastName("Markov");

        PupilDto pupilDtoTwo = new PupilDto();
        pupilDtoTwo.setId(2L);
        pupilDtoTwo.setFirstName("Masha");
        pupilDtoTwo.setLastName("Sidorova");

        when(pupilDAO.findPupilsByTeacherId(teacherId)).thenReturn(pupils);
        when(pupilMapper.pupilToPupilDto(pupilOne)).thenReturn(pupilDtoOne);
        when(pupilMapper.pupilToPupilDto(pupilTwo)).thenReturn(pupilDtoTwo);

        List<PupilDto> pupilDtos = pupilService.getAllPupilsByTeacher(teacherId);

        assertNotNull(pupilDtos);
        assertEquals(2, pupilDtos.size());
        assertEquals("Vladimir", pupilDtos.get(0).getFirstName());
        assertEquals("Sidorova", pupilDtos.get(1).getLastName());
    }

    @Test
    @DisplayName("PupilService remove Pupil from Teacher test")
    void removePupilFromTeacher() throws SQLException {
        pupilService.removePupilFromTeacher(1L, 1L);

        verify(pupilDAO, times(1)).deletePupilFromTeacherList(1L, 1L);
    }

    @Test
    @DisplayName("PupilService get Teacher list by Pupuil ID test")
    void getAllTeachersByPupil() throws SQLException {
        Long pupilId = 1L;

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

        when(pupilDAO.findTeachersByPupilId(pupilId)).thenReturn(teachers);
        when(teacherMapper.teacherToTeacherDto(teacherOne)).thenReturn(teacherDtoOne);
        when(teacherMapper.teacherToTeacherDto(teacherTwo)).thenReturn(teacherDtoTwo);

        List<TeacherDto> teacherDtos = pupilService.getAllTeachersByPupil(pupilId);

        assertNotNull(teacherDtos);
        assertEquals(2, teacherDtos.size());
        assertEquals("Ivan", teacherDtos.get(0).getFirstName());
        assertEquals("Kuznecov", teacherDtos.get(1).getLastName());
    }
}