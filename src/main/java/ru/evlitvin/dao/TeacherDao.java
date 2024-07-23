package ru.evlitvin.dao;

import ru.evlitvin.dao.interfaces.CrudDAOInterface;
import ru.evlitvin.entity.School;
import ru.evlitvin.entity.Teacher;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDao implements CrudDAOInterface<Teacher> {

    private final DataSource dataSource;

    public TeacherDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String INSERT = "INSERT INTO teacher (first_name, last_name, school_id) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID = "SELECT t.id, t.first_name, t.last_name, t.school_id, s.name, s.address " +
            "FROM teacher t JOIN school s ON t.school_id = s.id WHERE t.id = ?";
    private static final String FIND_ALL = "SELECT t.id, t.first_name, t.last_name, t.school_id, s.name, s.address " +
            "FROM teacher t JOIN school s ON t.school_id = s.id";
    private static final String UPDATE = "UPDATE teacher SET first_name = ?, last_name = ?, school_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM teacher WHERE id = ?";

    @Override
    public void insert(Teacher teacher) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, teacher.getFirstName());
            statement.setString(2, teacher.getLastName());
            statement.setLong(3, teacher.getSchool().getId());
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                teacher.setId(resultSet.getLong(1));
            }
        }
    }

    @Override
    public List<Teacher> findAll() throws SQLException {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(FIND_ALL);
            while (resultSet.next()) {
                Teacher teacher = new Teacher();
                setTeacherData(teacher, resultSet);

                School school = new School();
                setSchoolData(school, resultSet);
                teacher.setSchool(school);

                teachers.add(teacher);
            }
        }
        return teachers;
    }

    @Override
    public Teacher findById(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Teacher teacher = new Teacher();
                setTeacherData(teacher, resultSet);

                School school = new School();
                setSchoolData(school, resultSet);
                teacher.setSchool(school);

                return teacher;
            }
        }
        return null;
    }

    @Override
    public void update(Teacher teacher) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, teacher.getFirstName());
            statement.setString(2, teacher.getLastName());
            statement.setLong(3, teacher.getSchool().getId());
            statement.setLong(4, teacher.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(DELETE);
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    private void setTeacherData(Teacher teacher, ResultSet resultSet) throws SQLException {
        teacher.setId(resultSet.getLong("id"));
        teacher.setFirstName(resultSet.getString("first_name"));
        teacher.setLastName(resultSet.getString("last_name"));
    }

    private void setSchoolData(School school, ResultSet resultSet) throws SQLException {
        school.setId(resultSet.getLong("school_id"));
        school.setName(resultSet.getString("name"));
        school.setAddress(resultSet.getString("address"));
    }

}
