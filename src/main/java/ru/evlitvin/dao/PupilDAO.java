package ru.evlitvin.dao;

import ru.evlitvin.dao.interfaces.CrudDAOInterface;
import ru.evlitvin.dao.interfaces.PupilDAOInterface;
import ru.evlitvin.entity.Pupil;
import ru.evlitvin.entity.Teacher;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PupilDAO implements CrudDAOInterface<Pupil>, PupilDAOInterface {

    private final DataSource dataSource;

    public PupilDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String INSERT = "INSERT INTO pupil (first_name, last_name) VALUES (?, ?)";
    private static final String FIND_ALL = "SELECT id, first_name, last_name FROM pupil";
    private static final String FIND_BY_ID = "SELECT id, first_name, last_name FROM pupil WHERE id = ?";
    private static final String UPDATE = "UPDATE pupil SET first_name = ?, last_name = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM pupil WHERE id = ?";
    private static final String INSERT_INTO_PUPIL_TEACHER = "INSERT INTO pupil_teacher (pupil_id, teacher_id) VALUES (?, ?)";
    private static final String FIND_PUPILS_BY_TEACHER = "SELECT p.* FROM pupil p INNER JOIN pupil_teacher pt ON p.id = pt.pupil_id WHERE pt.teacher_id = ?";
    private static final String DELETE_PUPIL_FROM_TEACHER = "DELETE FROM pupil_teacher WHERE pupil_id = ? AND teacher_id = ?";
    private static final String FIND_TEACHERS_BY_PUPIL = "SELECT t.* FROM  teacher t INNER JOIN pupil_teacher pt ON t.id = pt.teacher_id WHERE pt.pupil_id = ?";

    @Override
    public void insert(Pupil pupil) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pupil.getFirstName());
            statement.setString(2, pupil.getLastName());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pupil.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    @Override
    public List<Pupil> findAll() throws SQLException {
        List<Pupil> pupils = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL)) {
            while (resultSet.next()) {
                pupils.add(setPupilData(resultSet));
            }
        }
        return pupils;
    }

    @Override
    public Pupil findById(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return setPupilData(resultSet);
            }
        }
        return null;
    }

    @Override
    public void update(Pupil pupil) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, pupil.getFirstName());
            statement.setString(2, pupil.getLastName());
            statement.setLong(3, pupil.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void addPupilToTeacherList(Long pupilId, Long teacherId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_INTO_PUPIL_TEACHER)) {
            statement.setLong(1, pupilId);
            statement.setLong(2, teacherId);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Pupil> findPupilsByTeacherId(Long teacherId) throws SQLException {
        List<Pupil> pupils = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_PUPILS_BY_TEACHER)) {
            statement.setLong(1, teacherId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pupils.add(setPupilData(resultSet));
                }
            }
        }
        return pupils;
    }

    public void deletePupilFromTeacherList(Long pupilId, Long teacherId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PUPIL_FROM_TEACHER)) {
            statement.setLong(1, pupilId);
            statement.setLong(2, teacherId);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Teacher> findTeachersByPupilId(Long id) throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TEACHERS_BY_PUPIL)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Teacher teacher = new Teacher();
                    teacher.setId(resultSet.getLong("id"));
                    teacher.setFirstName(resultSet.getString("first_name"));
                    teacher.setLastName(resultSet.getString("last_name"));
                    teachers.add(teacher);
                }
            }
        }
        return teachers;
    }

    private Pupil setPupilData(ResultSet resultSet) throws SQLException {
        Pupil pupil = new Pupil();
        pupil.setId(resultSet.getLong("id"));
        pupil.setFirstName(resultSet.getString("first_name"));
        pupil.setLastName(resultSet.getString("last_name"));
        return pupil;
    }
}
