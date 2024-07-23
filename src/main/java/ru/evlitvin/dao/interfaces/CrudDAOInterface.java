package ru.evlitvin.dao.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface CrudDAOInterface<T> {

    void insert(T type) throws SQLException, ClassNotFoundException;
    List<T> findAll() throws SQLException, ClassNotFoundException;
    T findById(Long id) throws SQLException, ClassNotFoundException;
    void update(T type) throws SQLException, ClassNotFoundException;
    void delete(Long id) throws SQLException, ClassNotFoundException;

}
