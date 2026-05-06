package com.project.face.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB document representing an uploaded video.
 * 
 * Example MongoDB document:
 * {
 * "_id": "664f1a2b3c4d5e6f7a8b9c0d",
 * "fileName": "interview_clip.mp4",
 * "originalFileName": "interview_clip.mp4",
 * "filePath": "uploads/664f1a2b3c4d5e6f7a8b9c0d/interview_clip.mp4",
 * "contentType": "video/mp4",
 * "fileSize": 5242880,
 * "status": "PROCESSED",
 * "frameCount": 10,
 * "processedFramePaths": ["uploads/.../frame_0.png", ...],
 * "uploadedAt": "2024-05-23T10:30:00",
 * "processedAt": "2024-05-23T10:30:05"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "videos")
public class Video {

    @Id
    private String id;

    /** Original file name from the upload */
    private String fileName;

    /** Original file name preserved for display */
    private String originalFileName;

    /** Server-side path where the video file is stored */
    private String filePath;

    /** MIME type of the uploaded file (e.g., video/mp4) */
    private String contentType;

    /** File size in bytes */
    private long fileSize;

    /** Processing status: UPLOADED, PROCESSING, PROCESSED, FAILED */
    private String status;

    /** Number of frames extracted from the video */
    private int frameCount;

    /** Paths to processed frames with bounding boxes drawn */
    private List<String> processedFramePaths;

    /** Timestamp when the video was uploaded */
    private LocalDateTime uploadedAt;

    /** Timestamp when processing completed */
    private LocalDateTime processedAt;
}
