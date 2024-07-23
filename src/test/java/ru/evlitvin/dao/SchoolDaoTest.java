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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DisplayName("Tests for SchoolDao class")
class SchoolDaoTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("schooldb")
            .withUsername("postgres")
            .withPassword("password");

    private static DataSource dataSource;
    private static SchoolDao schoolDao;

    @BeforeAll
    public static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
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
    @DisplayName("SchoolDao insert School instance to school table test")
    void insert() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        School savedSchool = schoolDao.findById(school.getId());
        assertNotNull(savedSchool);
        assertEquals("School One name", savedSchool.getName());
        assertEquals("School One address", savedSchool.getAddress());
    }

    @Test
    @DisplayName("SchoolDao insert School instance to school table and return key test")
    void insertSchoolAndReturnGeneratedKey() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        assertNotEquals(0, school.getId());
    }

    @Test
    @DisplayName("SchoolDao find all schools with teachers test")
    void findAllSchoolsWithTeachers() throws SQLException {
        School schoolOne = new School();
        schoolOne.setName("School One name");
        schoolOne.setAddress("School One address");
        schoolDao.insert(schoolOne);

        School schoolTwo = new School();
        schoolTwo.setName("School Two name");
        schoolTwo.setAddress("School Two address");
        schoolDao.insert(schoolTwo);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO teacher (school_id, first_name, last_name) VALUES (" + schoolOne.getId() + ", 'Ivan', 'Ivanov')");
            statement.execute("INSERT INTO teacher (school_id, first_name, last_name) VALUES (" + schoolOne.getId() + ", 'Petr', 'Kuznecov')");
            statement.execute("INSERT INTO teacher (school_id, first_name, last_name) VALUES (" + schoolTwo.getId() + ", 'Ivan', 'Ivanov')");
        }

        List<School> schools = schoolDao.findAll();
        assertNotNull(schools);
        assertEquals(2, schools.size());

        School savedSchoolOne = schools.get(0);
        assertEquals("School One name", savedSchoolOne.getName());
        assertEquals("School One address", savedSchoolOne.getAddress());
        assertNotNull(savedSchoolOne.getTeachers());
        assertEquals(2, savedSchoolOne.getTeachers().size());

        Teacher teacherOne = savedSchoolOne.getTeachers().get(0);
        assertEquals("Ivan", teacherOne.getFirstName());
        assertEquals("Ivanov", teacherOne.getLastName());

        Teacher teacherTwo = savedSchoolOne.getTeachers().get(1);
        assertEquals("Petr", teacherTwo.getFirstName());
        assertEquals("Kuznecov", teacherTwo.getLastName());

        School savedSchoolTwo = schools.get(1);
        assertEquals("School Two name", savedSchoolTwo.getName());
        assertEquals("School Two address", savedSchoolTwo.getAddress());
        assertNotNull(savedSchoolTwo.getTeachers());
        assertEquals(1, savedSchoolTwo.getTeachers().size());

        Teacher teacherThree = savedSchoolTwo.getTeachers().get(0);
        assertEquals("Ivan", teacherThree.getFirstName());
        assertEquals("Ivanov", teacherThree.getLastName());
    }

    @Test
    @DisplayName("SchoolDao find school with teachers test")
    void findSchoolWithTeachers() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO teacher (school_id, first_name, last_name) VALUES (" + school.getId() + ", 'Ivan', 'Ivanov')");
            statement.execute("INSERT INTO teacher (school_id, first_name, last_name) VALUES (" + school.getId() + ", 'Petr', 'Kuznecov')");
        }

        School savedSchool = schoolDao.findById(school.getId());
        assertNotNull(savedSchool);
        assertEquals("School One name", savedSchool.getName());
        assertEquals("School One address", savedSchool.getAddress());
        assertNotNull(savedSchool.getTeachers());
        assertEquals(2, savedSchool.getTeachers().size());

        Teacher teacherOne = savedSchool.getTeachers().get(0);
        assertEquals("Ivan", teacherOne.getFirstName());
        assertEquals("Ivanov", teacherOne.getLastName());

        Teacher teacherTwo = savedSchool.getTeachers().get(1);
        assertEquals("Petr", teacherTwo.getFirstName());
        assertEquals("Kuznecov", teacherTwo.getLastName());
    }

    @Test
    @DisplayName("SchoolDao find school by ID and return instance test")
    void findByIdReturnInstanceTest() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        School savedSchool = schoolDao.findById(school.getId());

        assertNotNull(savedSchool);
        assertEquals("School One name", savedSchool.getName());
        assertEquals("School One address", savedSchool.getAddress());
    }

    @Test
    @DisplayName("SchoolDao find school by not existed ID and return null test")
    void findByIdReturnNullTest() throws SQLException {
        School school = new School();
        school.setId(1L);
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        School savedSchool = schoolDao.findById(school.getId());

        assertNotNull(savedSchool);
        assertEquals("School One name", savedSchool.getName());
        assertEquals("School One address", savedSchool.getAddress());

        School savedNullSchool = schoolDao.findById(15L);

        assertNull(savedNullSchool);
    }

    @Test
    @DisplayName("SchoolDao update school test")
    void updateTest() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        School savedSchool = schoolDao.findById(school.getId());
        savedSchool.setName("School One another name");
        savedSchool.setAddress("School One another address");
        schoolDao.update(savedSchool);

        assertEquals("School One another name", savedSchool.getName());
        assertEquals("School One another address", savedSchool.getAddress());
        assertEquals(savedSchool.getId(), schoolDao.findById(school.getId()).getId());
    }

    @Test
    @DisplayName("SchoolDao delete school by ID test")
    void deleteTest() throws SQLException {
        School school = new School();
        school.setName("School One name");
        school.setAddress("School One address");
        schoolDao.insert(school);

        School savedSchool = schoolDao.findById(school.getId());
        schoolDao.delete(savedSchool.getId());
        List<School> schools = schoolDao.findAll();

        assertNull(schoolDao.findById(school.getId()));
        assertEquals(0, schools.size());
    }
}