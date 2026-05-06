package com.project.face.repository;

import com.project.face.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Video documents.
 * Spring Data MongoDB automatically provides CRUD operations.
 */
@Repository
public interface VideoRepository extends MongoRepository<Video, String> {
    // Spring Data MongoDB provides all basic CRUD methods:
    // save(), findById(), findAll(), deleteById(), etc.
    // No custom queries needed for this simple use case.
}
