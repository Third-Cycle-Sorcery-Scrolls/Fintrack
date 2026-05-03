package repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entity);               // insert a new entity, return it with generated ID
    List<T> findAll();              // get all entities
    Optional<T> findById(ID id);    // find one by ID (maybe not exist)
    void update(T entity);          // update existing entity
    void deleteById(ID id);         // delete by ID
}