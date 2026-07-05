package com.taxtracker.repository;

import com.taxtracker.entity.UploadedDocumentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadedDocumentRepository extends CrudRepository<UploadedDocumentEntity, Long> {

    Optional<UploadedDocumentEntity> findTopByEmailOrderByDocumentIdDesc(String email);
}
