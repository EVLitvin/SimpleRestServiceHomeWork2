package ru.evlitvin.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.evlitvin.entity.Pupil;
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
@DisplayName("Tests for PupilDAO class")
class PupilDAOTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("schooldb")
            .withUsername("postgres")
            .withPassword("password");

    private static DataSource dataSource;
    private static PupilDAO pupilDAO;
    private static TeacherDao teacherDao;

    @BeforeAll
    public static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        config.setUsername(postgreSQLContainer.getUsername());
        config.setPassword(postgreSQLContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
        pupilDAO = new PupilDAO(dataSource);
        teacherDao = new TeacherDao(dataSource);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS pupil" +
                    "(id BIGSERIAL PRIMARY KEY," +
                    "first_name TEXT NOT NULL," +
                    "last_name TEXT NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS teacher" +
                    "(id BIGSERIAL PRIMARY KEY," +
                    "first_name TEXT NOT NULL," +
                    "last_name TEXT NOT NULL," +
                    "school_id BIGINT NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS pupil_teacher" +
                    "(id BIGSERIAL PRIMARY KEY," +
                    "pupil_id INTEGER NOT NULL REFERENCES pupil," +
                    "teacher_id INTEGER NOT NULL REFERENCES teacher," +
                    "UNIQUE (teacher_id, pupil_id));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void cleanData() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        config.setUsername(postgreSQLContainer.getUsername());
        config.setPassword(postgreSQLContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
        pupilDAO = new PupilDAO(dataSource);
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM pupil_teacher");
            stmt.execute("DELETE FROM pupil");
            stmt.execute("DELETE FROM teacher");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void start() {
        postgreSQLContainer.start();
    }


    @AfterAll
    public static void teardown() {
        postgreSQLContainer.stop();
    }


    @Test
    @DisplayName("PupilDao insert Pupil instance to pupil table test")
    void insert() throws SQLException {
        Pupil pupil = new Pupil();
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markin");

        pupilDAO.insert(pupil);

        Pupil savedPupil = pupilDAO.findById(pupil.getId());

        assertEquals("Vladimir", savedPupil.getFirstName());
        assertEquals("Markin", savedPupil.getLastName());
    }

    @Test
    @DisplayName("PupilDao insert Pupil instance return generated key test")
    void insertPupilWithGeneratedKeyTest() throws SQLException {
        Pupil pupil = new Pupil();
        pupil.setId(1L);
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markin");

        pupilDAO.insert(pupil);

        assertNotEquals(0, pupil.getId());
    }

    @Test
    @DisplayName("PupilDao insert Pupil instance without generated key test")
    void testSaveWithoutGeneratedKeys() throws SQLException {
        Pupil pupil = new Pupil();
        pupil.setId(1L);
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markin");

        pupilDAO.insert(pupil);

        DataSource mockDataSource = mock(DataSource.class);
        PupilDAO mockPupilDao = new PupilDAO(mockDataSource);

        try (Connection mockConnection = mock(Connection.class);
             PreparedStatement statement = mock(PreparedStatement.class);
             ResultSet resultSet = mock(ResultSet.class)) {

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS))).thenReturn(statement);
            when(statement.getGeneratedKeys()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            Pupil mockPupil = new Pupil();
            mockPupilDao.insert(mockPupil);

            assertNull(mockPupil.getId());
        }
    }

    @Test
    void findAll() throws SQLException {
        Pupil pupilOne = new Pupil();
        pupilOne.setFirstName("Vladimir");
        pupilOne.setLastName("Markin");

        Pupil pupilTwo = new Pupil();
        pupilTwo.setFirstName("Masha");
        pupilTwo.setLastName("Sidorova");

        pupilDAO.insert(pupilOne);
        pupilDAO.insert(pupilTwo);

        List<Pupil> pupils = pupilDAO.findAll();

        assertNotNull(pupils);
        assertEquals(2, pupils.size());
        assertEquals("Vladimir", pupils.get(0).getFirstName());
        assertEquals("Sidorova", pupils.get(1).getLastName());
    }

    @Test
    @DisplayName("PupilDao find by Pupil ID test")
    void findById() throws SQLException {
        Pupil pupilOne = new Pupil();
        pupilOne.setId(1L);
        pupilOne.setFirstName("Vladimir");
        pupilOne.setLastName("Markin");
        pupilDAO.insert(pupilOne);

        Pupil pupilTwo = new Pupil();
        pupilTwo.setId(2L);
        pupilTwo.setFirstName("Masha");
        pupilTwo.setLastName("Sidorova");
        pupilDAO.insert(pupilTwo);

        Pupil savedPupilOne = pupilDAO.findById(pupilOne.getId());
        assertNotNull(savedPupilOne);
        assertEquals("Vladimir", savedPupilOne.getFirstName());
        assertEquals("Markin", savedPupilOne.getLastName());

        Pupil savedPupilTwo = pupilDAO.findById(pupilTwo.getId());
        assertNotNull(savedPupilTwo);
        assertEquals("Masha", savedPupilTwo.getFirstName());
        assertEquals("Sidorova", savedPupilTwo.getLastName());
    }

    @Test
    @DisplayName("PupilDao find by pupil ID return null test")
    void findPupilByIdReturnNull() throws SQLException {
        Pupil pupil = new Pupil();
        pupil.setId(1L);
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markin");
        pupilDAO.insert(pupil);

        Pupil savedPupil = pupilDAO.findById(pupil.getId());
        assertNotNull(savedPupil);
        assertEquals("Vladimir", savedPupil.getFirstName());
        assertEquals("Markin", savedPupil.getLastName());

        Pupil savedPupilNull = pupilDAO.findById(10L);
        assertNull(savedPupilNull);
    }

    @Test
    void update() throws SQLException {
        Pupil pupil = new Pupil();
        pupil.setId(1L);
        pupil.setFirstName("Vladimir");
        pupil.setLastName("Markin");
        pupilDAO.insert(pupil);

        List<Pupil> pupils = pupilDAO.findAll();
        Pupil savedPupil = pupils.get(0);

        savedPupil.setFirstName("Maksim");

        pupilDAO.update(savedPupil);

        Pupil updatedPupil = pupilDAO.findById(savedPupil.getId());
        assertEquals("Maksim", updatedPupil.getFirstName());


    }

    @Test
    void delete() throws SQLException {
        Pupil pupilOne = new Pupil();
        pupilOne.setFirstName("Vladimir");
        pupilOne.setLastName("Markin");

        pupilDAO.insert(pupilOne);

        assertEquals(1, pupilDAO.findAll().size());

        pupilDAO.delete(pupilOne.getId());

        assertEquals(0, pupilDAO.findAll().size());
    }

    @Test
    void addPupilToTeacherList() throws SQLException {
        Pupil pupil = new Pupil(1L, "Vladimir", "Markin");
        pupilDAO.insert(pupil);

        School school = new School(1L, "School name", "School address");

        Teacher teacher = new Teacher(1L, "Ivan", "Ivanov", school);
        teacherDao.insert(teacher);

        pupilDAO.addPupilToTeacherList(pupil.getId(), teacher.getId());

        List<Pupil> pupils = pupilDAO.findPupilsByTeacherId(teacher.getId());
        assertEquals(1, pupils.size());
        assertEquals(pupil.getId(), pupils.get(0).getId());

        List<Teacher> teachers = pupilDAO.findTeachersByPupilId(pupil.getId());
        assertEquals(1, teachers.size());
        assertEquals(teacher.getId(), teachers.get(0).getId());
    }

    @Test
    void deletePupilFromTeacherList() throws SQLException {
        Pupil pupil = new Pupil(1L, "Vladimir", "Markin");
        pupilDAO.insert(pupil);

        School school = new School(1L, "School name", "School address");

        Teacher teacher = new Teacher(1L, "Ivan", "Ivanov", school);
        teacherDao.insert(teacher);

        pupilDAO.addPupilToTeacherList(pupil.getId(), teacher.getId());

        pupilDAO.deletePupilFromTeacherList(pupil.getId(), teacher.getId());

        List<Pupil> pupils = pupilDAO.findPupilsByTeacherId(teacher.getId());
        Assertions.assertEquals(0, pupils.size());

        List<Teacher> teachers = pupilDAO.findTeachersByPupilId(pupil.getId());
        Assertions.assertEquals(0, teachers.size());
    }

}