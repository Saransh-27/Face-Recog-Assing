package com.project.face.repository;

import com.project.face.model.ROI;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ROI (Region of Interest) documents.
 * 
 * The findByVideoId method leverages the @Indexed annotation on ROI.videoId
 * for efficient queries. MongoDB will use the index:
 * db.rois.createIndex({ "videoId": 1 })
 */
@Repository
public interface ROIRepository extends MongoRepository<ROI, String> {

    /**
     * Find all ROI entries associated with a specific video.
     * This is the primary query pattern — fetching all face detections for a video.
     *
     * @param videoId the ID of the parent video
     * @return list of ROI documents for that video
     */
    List<ROI> findByVideoId(String videoId);
}
