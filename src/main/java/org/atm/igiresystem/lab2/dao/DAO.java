package org.atm.igiresystem.lab2.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface providing standard CRUD operations.
 * All DAO classes implement this interface.
 *
 * @param <T>  the entity type
 */
public interface DAO<T> {

    void create(T obj);

    Optional<T> findById(int id);

    List<T> findAll();

    void update(T obj);

    void delete(int id);
}
