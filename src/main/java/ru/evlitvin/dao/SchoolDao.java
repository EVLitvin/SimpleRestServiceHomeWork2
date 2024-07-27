package ru.evlitvin.dao;

import ru.evlitvin.dao.interfaces.CrudDAOInterface;
import ru.evlitvin.entity.School;
import ru.evlitvin.entity.Teacher;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolDao implements CrudDAOInterface<School> {

    private final DataSource dataSource;

    public SchoolDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String INSERT = "INSERT INTO school(name, address) VALUES (?,?)";
    private static final String FIND_BY_ID = "SELECT s.id, s.name, s.address, s.id as teacher_id, t.first_name, t.last_name " +
            "FROM school s LEFT JOIN teacher t ON s.id = t.school_id WHERE s.id = ?";
    private static final String FIND_ALL = "SELECT s.id, s.name, s.address, s.id as teacher_id, t.first_name, t.last_name " +
            "FROM school s LEFT JOIN teacher t ON s.id = t.school_id";
    private static final String UPDATE = "UPDATE school SET name = ?, address = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM school WHERE id = ?";

    @Override
    public void insert(School school) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, school.getName());
            statement.setString(2, school.getAddress());
            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                school.setId(resultSet.getLong(1));
            }
        }
    }

    @Override
    public List<School> findAll() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            ResultSet resultSet = statement.executeQuery();
            List<School> schools = new ArrayList<>();
            School currentSchool = null;
            while (resultSet.next()) {
                Long schoolId = resultSet.getLong("id");
                if (currentSchool == null || !currentSchool.getId().equals(schoolId)) {
                    currentSchool = new School();
                    currentSchool.setId(schoolId);
                    currentSchool.setName(resultSet.getString("name"));
                    currentSchool.setAddress(resultSet.getString("address"));
                    currentSchool.setTeachers(new ArrayList<>());
                    schools.add(currentSchool);
                }
                if (resultSet.getLong("teacher_id") != 0) {
                    Teacher teacher = new Teacher();
                    teacher.setId(resultSet.getLong("teacher_id"));
                    teacher.setFirstName(resultSet.getString("first_name"));
                    teacher.setLastName(resultSet.getString("last_name"));
                    teacher.setSchool(currentSchool);
                    currentSchool.getTeachers().add(teacher);
                }
            }
            return schools;
        }
    }

    @Override
    public School findById(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            School school = null;
            List<Teacher> teachers = new ArrayList<>();
            while (resultSet.next()) {
                if (school == null) {
                    school = new School();
                    school.setId(resultSet.getLong("id"));
                    school.setName(resultSet.getString("name"));
                    school.setAddress(resultSet.getString("address"));
                }
                if (resultSet.getLong("teacher_id") != 0) {
                    Teacher teacher = new Teacher();
                    teacher.setId(resultSet.getLong("teacher_id"));
                    teacher.setFirstName(resultSet.getString("first_name"));
                    teacher.setLastName(resultSet.getString("last_name"));
                    teacher.setSchool(school);
                    teachers.add(teacher);
                }
            }
            if (school != null) {
                school.setTeachers(teachers);
            }
            return school;
        }
    }

    @Override
    public void update(School school) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, school.getName());
            statement.setString(2, school.getAddress());
            statement.setLong(3, school.getId());
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

}
