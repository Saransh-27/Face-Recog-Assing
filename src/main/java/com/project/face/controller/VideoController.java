package com.project.face.controller;

import com.project.face.model.ROI;
import com.project.face.model.Video;
import com.project.face.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for video processing API.
 * 
 * Endpoints:
 * POST /api/video/upload — Upload a video for processing
 * GET /api/video/{id} — Get video details and processed frames
 * GET /api/video/{id}/roi — Get all ROI (face detection) data for a video
 * GET /api/video/{id}/frames/{frameNumber} — Download a specific processed
 * frame
 * 
 * Architecture: Controller → VideoService → Repository
 */
@Slf4j
@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*") // Allow frontend access (configure properly in production)
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * POST /api/video/upload
     * 
     * Upload a video file for face detection processing.
     * The video will be:
     * 1. Saved to disk
     * 2. Processed into frames
     * 3. Each frame analyzed for face detection
     * 4. Bounding boxes drawn on detected faces
     * 5. ROI data stored in MongoDB
     *
     * @param file the video file (multipart form-data)
     * @return Video document with processing results
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        log.info("Received video upload: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file provided", "message", "Please upload a video file"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            log.warn("Invalid file type uploaded: {}", contentType);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file type",
                            "message", "Only video files are accepted (video/mp4, video/avi, etc.)"));
        }

        try {
            Video video = videoService.uploadVideo(file);
            log.info("Video upload and processing complete. ID: {}, Status: {}",
                    video.getId(), video.getStatus());
            Map<String, Object> response = new HashMap<>();
            response.put("video", video);
            response.put("message", "Video uploaded and processed successfully");
            response.put("framesProcessed", video.getFrameCount());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Failed to upload video", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed", "message", e.getMessage()));
        }
    }

    /**
     * GET /api/video/{id}
     * 
     * Retrieve video details including processing status and frame information.
     *
     * @param id the MongoDB video document ID
     * @return Video document or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVideo(@PathVariable String id) {
        log.info("Fetching video details for ID: {}", id);
        Optional<Video> video = videoService.getVideoById(id);
        if (video.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Video not found",
                            "message", "No video found with ID: " + id));
        }
        return ResponseEntity.ok(video.get());
    }

    /**
     * GET /api/video/{id}/roi
     * 
     * Retrieve all ROI (Region of Interest) data for a specific video.
     * Returns bounding box coordinates for every detected face in every frame.
     *
     * @param id the MongoDB video document ID
     * @return list of ROI documents with bounding box data
     */
    @GetMapping("/{id}/roi")
    public ResponseEntity<?> getROIs(@PathVariable String id) {
        log.info("Fetching ROI data for video ID: {}", id);
        Optional<Video> video = videoService.getVideoById(id);
        if (video.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Video not found",
                            "message", "No video found with ID: " + id));
        }
        List<ROI> rois = videoService.getROIsByVideoId(id);
        Map<String, Object> response = new HashMap<>();
        response.put("videoId", id);
        response.put("totalFrames", video.get().getFrameCount());
        response.put("totalDetections", rois.size());
        response.put("rois", rois);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/video/{id}/frames/{frameNumber}
     * 
     * Download a specific processed frame (with bounding boxes drawn).
     * This allows the frontend to display individual frames.
     *
     * @param id          the video ID
     * @param frameNumber the frame number (0-based)
     * @return the frame image as PNG
     */
    @GetMapping("/{id}/frames/{frameNumber}")
    public ResponseEntity<Resource> getFrame(@PathVariable String id,
            @PathVariable int frameNumber) {
        log.info("Fetching frame {} for video ID: {}", frameNumber, id);
        Optional<Video> videoOpt = videoService.getVideoById(id);
        if (videoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Video video = videoOpt.get();
        if (frameNumber < 0 || frameNumber >= video.getFrameCount()) {
            return ResponseEntity.badRequest().build();
        }
        String framePath = video.getProcessedFramePaths().get(frameNumber);
        File frameFile = new File(framePath);
        if (!frameFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(frameFile);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"frame_" + frameNumber + ".png\"")
                .body(resource);
    }
}
