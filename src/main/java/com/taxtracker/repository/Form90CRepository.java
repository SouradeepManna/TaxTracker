package com.taxtracker.repository;

import com.taxtracker.entity.Form90CEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Form90CRepository extends CrudRepository<Form90CEntity, Long> {

    Optional<Form90CEntity> findByEmail(String email);

    Optional<Form90CEntity> findTopByEmailOrderByFormIdDesc(String email);
}
