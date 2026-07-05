package com.taxtracker.repository;

import com.taxtracker.entity.PendingRegistrationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends CrudRepository<PendingRegistrationEntity, Long> {

    Optional<PendingRegistrationEntity> findByEmail(String email);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);
}
