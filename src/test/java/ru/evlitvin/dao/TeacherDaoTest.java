package ru.evlitvin.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.evlitvin.entity.School;
import ru.evlitvin.entity.Teacher;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
class TeacherDaoTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("schooldb")
            .withUsername("postgres")
            .withPassword("password");

    private static DataSource dataSource;

    private static TeacherDao teacherDao;

    private static SchoolDao schoolDao;

    @BeforeAll
    public static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
        teacherDao = new TeacherDao(dataSource);
        schoolDao = new SchoolDao(dataSource);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS school" +
                    "(id  BIGSERIAL PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "address TEXT NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS teacher" +
                    "(id BIGSERIAL PRIMARY KEY," +
                    "first_name TEXT NOT NULL," +
                    "last_name TEXT NOT NULL," +
                    "school_id  BIGINT NOT NULL);");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void cleanData() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM teacher");
            statement.execute("DELETE FROM school");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void start() {
        postgresContainer.start();
    }


    @AfterAll
    public static void teardown() {
        postgresContainer.stop();
    }

    @Test
    @DisplayName("TeacherDao insert Teacher  instance to teacher table test")
    void insert() throws SQLException {
        School school = new School();
        school.setId(1L);
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        Teacher teacher = new Teacher();
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivanov");
        teacher.setSchool(school);
        teacherDao.insert(teacher);

        Teacher savedTeacher = teacherDao.findById(teacher.getId());
        assertNotNull(savedTeacher);
        assertEquals("Ivan", savedTeacher.getFirstName());
        assertEquals("Ivanov", savedTeacher.getLastName());
        assertTrue(teacher.getId() > 0);
    }

    @Test
    @DisplayName("TeacherDao find by Teacher ID test")
    void findTeacherById() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        Teacher teacherOne = new Teacher();
        teacherOne.setFirstName("Ivan");
        teacherOne.setLastName("Ivanov");
        teacherOne.setSchool(school);
        teacherDao.insert(teacherOne);

        Teacher teacherTwo = new Teacher();
        teacherTwo.setFirstName("Petr");
        teacherTwo.setLastName("Kuznecov");
        teacherTwo.setSchool(school);
        teacherDao.insert(teacherTwo);

        Teacher savedTeacherOne = teacherDao.findById(teacherOne.getId());
        assertNotNull(savedTeacherOne);
        assertEquals("Ivan", savedTeacherOne.getFirstName());
        assertEquals("Ivanov", savedTeacherOne.getLastName());

        Teacher savedTeacherTwo = teacherDao.findById(teacherTwo.getId());
        assertNotNull(savedTeacherTwo);
        assertEquals("Petr", savedTeacherTwo.getFirstName());
        assertEquals("Kuznecov", savedTeacherTwo.getLastName());
    }

    @Test
    @DisplayName("TeacherDao find by Teacher ID return null test")
    void findTeacherByIdReturnNull() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        Teacher teacherOne = new Teacher();
        teacherOne.setFirstName("Ivan");
        teacherOne.setLastName("Ivanov");
        teacherOne.setSchool(school);
        teacherDao.insert(teacherOne);

        Teacher savedTeacher = teacherDao.findById(teacherOne.getId());
        assertNotNull(savedTeacher);
        assertEquals("Ivan", savedTeacher.getFirstName());
        assertEquals("Ivanov", savedTeacher.getLastName());

        Teacher teacherWithNullId = teacherDao.findById(10L);
        assertNull(teacherWithNullId);
    }

    @Test
    @DisplayName("TeacherDao insert Teacher instance return generated key test")
    void insertTeacherWithGeneratedKeyTest() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        Teacher teacher = new Teacher();
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivanov");
        teacher.setSchool(school);
        teacherDao.insert(teacher);

        assertNotEquals(0, teacher.getId());
    }

    @Test
    @DisplayName("TeacherDao insert Teacher instance without generated key test")
    void insertTeacherWithoutGeneratedKeyTest() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        DataSource mockDataSource = mock(DataSource.class);
        TeacherDao mockTeacherDao = new TeacherDao(mockDataSource);

        try (Connection mockConnection = mock(Connection.class);
             PreparedStatement mockStmt = mock(PreparedStatement.class);
             ResultSet mockRs = mock(ResultSet.class)) {

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
            when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            Teacher mockedTeacher = new Teacher();
            mockedTeacher.setFirstName("Ivan");
            mockedTeacher.setLastName("Ivanov");
            mockedTeacher.setSchool(school);

            mockTeacherDao.insert(mockedTeacher);

            assertNull(mockedTeacher.getId());
        }
    }

    @Test
    @DisplayName("TeacherDao update Teacher instance test")
    void update() throws SQLException {
        School school = new School();
        school.setId(1L);
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        Teacher teacher = new Teacher();
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivanov");
        teacher.setSchool(school);
        teacherDao.insert(teacher);

        List<Teacher> teachers = teacherDao.findAll();
        Teacher savedTeacher = teachers.get(0);

        savedTeacher.setFirstName("Petr");
        savedTeacher.setLastName("Kuznecov");

        teacherDao.update(savedTeacher);

        Teacher updatedTeacher = teacherDao.findById(savedTeacher.getId());
        assertEquals("Petr", updatedTeacher.getFirstName());
        assertEquals("Kuznecov", updatedTeacher.getLastName());
    }

    @Test
    @DisplayName("TeacherDao delete Teacher instance by ID test")
    void delete() throws SQLException {
        School school = new School();
        school.setId(1L);
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        Teacher teacher = new Teacher();
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivanov");
        teacher.setSchool(school);
        teacherDao.insert(teacher);

        List<Teacher> teachers = teacherDao.findAll();
        assertEquals(1, teachers.size());

        Teacher savedTeacher = teachers.get(0);

        teacherDao.delete(savedTeacher.getId());

        assertTrue(teacherDao.findAll().isEmpty());
    }
}