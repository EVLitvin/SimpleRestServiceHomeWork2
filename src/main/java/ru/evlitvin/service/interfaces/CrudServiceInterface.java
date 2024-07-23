package ru.evlitvin.service.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface CrudServiceInterface<T> {

    void save(T type) throws SQLException, ClassNotFoundException;
    List<T> getAll() throws SQLException, ClassNotFoundException;
    T getById(Long id) throws SQLException, ClassNotFoundException;
    void update(T type) throws SQLException, ClassNotFoundException;
    void delete(Long id) throws SQLException, ClassNotFoundException;

}
