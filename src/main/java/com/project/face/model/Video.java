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
    private String fileName;
    private String originalFileName;
    private String filePath;
    private String contentType;
    private long fileSize;
    private String status;
    private int frameCount;
    private List<String> processedFramePaths;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
}
