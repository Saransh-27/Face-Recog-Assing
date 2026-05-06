package com.project.face.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a Region of Interest (ROI) detected in a video
 * frame.
 * Each ROI contains bounding box coordinates for a detected face.
 * 
 * Index: videoId is indexed for fast lookups when querying all ROIs for a
 * video.
 * 
 * Example MongoDB document:
 * {
 * "_id": "664f1b3c4d5e6f7a8b9c0d1e",
 * "videoId": "664f1a2b3c4d5e6f7a8b9c0d",
 * "frameNumber": 0,
 * "x": 120,
 * "y": 80,
 * "width": 150,
 * "height": 180,
 * "confidence": 0.92,
 * "label": "face"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rois")
public class ROI {

    @Id
    private String id;

    /**
     * Reference to the parent Video document.
     * Indexed for efficient querying — see ROIRepository.
     * 
     * Index suggestion: db.rois.createIndex({ "videoId": 1 })
     */
    @Indexed
    private String videoId;

    /** Frame number within the video (0-based) */
    private int frameNumber;

    /** X coordinate of the bounding box top-left corner */
    private int x;

    /** Y coordinate of the bounding box top-left corner */
    private int y;

    /** Width of the bounding box in pixels */
    private int width;

    /** Height of the bounding box in pixels */
    private int height;

    /**
     * Detection confidence score (0.0 to 1.0) — mock value in this implementation
     */
    private double confidence;

    /** Label for the detected object (always "face" in this system) */
    private String label;
}
