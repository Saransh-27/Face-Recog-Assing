package com.project.face.service;

import com.project.face.model.ROI;
import com.project.face.model.Video;
import com.project.face.repository.ROIRepository;
import com.project.face.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for video operations.
 * Handles video upload, storage, and retrieval.
 * Delegates frame processing to FrameProcessingService.
 * 
 * Architecture: Controller → VideoService → Repository
 * → FrameProcessingService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final ROIRepository roiRepository;
    private final FrameProcessingService frameProcessingService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Handles the complete video upload workflow:
     * 1. Save the uploaded file to disk
     * 2. Create a Video document in MongoDB
     * 3. Process the video (extract frames, detect faces, draw bounding boxes)
     * 4. Update the Video document with processing results
     *
     * @param file the uploaded video file
     * @return the saved Video document with processing results
     */
    public Video uploadVideo(MultipartFile file) throws IOException {
        log.info("Uploading video: {}", file.getOriginalFilename());

        // Step 1: Create initial Video document with UPLOADED status
        Video video = Video.builder()
                .fileName(file.getOriginalFilename())
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .status("UPLOADED")
                .uploadedAt(LocalDateTime.now())
                .build();

        // Save to get a MongoDB-generated ID
        video = videoRepository.save(video);
        log.info("Video document created with ID: {}", video.getId());

        // Step 2: Save the actual video file to disk
        // IMPORTANT: Use toAbsolutePath() so file.transferTo() doesn't resolve
        // against Tomcat's temp directory, which causes FileNotFoundException
        Path videoDir = Paths.get(uploadDir, video.getId()).toAbsolutePath();
        Files.createDirectories(videoDir);
        Path videoPath = videoDir.resolve(file.getOriginalFilename());
        file.transferTo(videoPath.toFile());
        video.setFilePath(videoPath.toString());

        // Step 3: Update status to PROCESSING
        video.setStatus("PROCESSING");
        videoRepository.save(video);

        try {
            // Step 4: Process the video — extract frames, detect faces, draw boxes
            List<String> processedFramePaths = frameProcessingService.processVideo(
                    videoPath.toString(), video.getId(), videoDir.toString());

            // Step 5: Update video with results
            video.setStatus("PROCESSED");
            video.setFrameCount(processedFramePaths.size());
            video.setProcessedFramePaths(processedFramePaths);
            video.setProcessedAt(LocalDateTime.now());
            log.info("Video processed successfully. Frames: {}", processedFramePaths.size());

        } catch (Exception e) {
            log.error("Video processing failed for ID: {}", video.getId(), e);
            video.setStatus("FAILED");
        }

        return videoRepository.save(video);
    }

    /**
     * Retrieve a video by its ID.
     */
    public Optional<Video> getVideoById(String id) {
        return videoRepository.findById(id);
    }

    /**
     * Retrieve all ROI data for a specific video.
     * Uses the indexed videoId field for efficient lookup.
     */
    public List<ROI> getROIsByVideoId(String videoId) {
        return roiRepository.findByVideoId(videoId);
    }
}
