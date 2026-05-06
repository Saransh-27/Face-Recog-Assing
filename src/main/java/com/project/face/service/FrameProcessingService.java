package com.project.face.service;

import com.project.face.model.ROI;
import com.project.face.repository.ROIRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for processing video frames.
 * 
 * This service handles:
 * 1. Frame extraction (simulated — generates colored frames since we avoid
 * heavy video libs)
 * 2. Face detection (mock logic — generates realistic bounding boxes)
 * 3. Bounding box drawing using Java's Graphics2D (NO OpenCV, NO Python)
 * 4. Storing ROI data in MongoDB
 * 
 * WHY SIMULATED?
 * - Real video frame extraction requires FFmpeg or JavaCV (heavy dependencies)
 * - Real face detection requires ML models (dlib, OpenCV DNN, etc.)
 * - This implementation provides a clean, working API layer that can be
 * swapped out with real implementations later (see README for extension plan)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FrameProcessingService {

    private final ROIRepository roiRepository;

    // Number of simulated frames to extract per video
    private static final int SIMULATED_FRAME_COUNT = 10;

    // Simulated frame dimensions (standard 720p)
    private static final int FRAME_WIDTH = 1280;
    private static final int FRAME_HEIGHT = 720;

    /**
     * Process a video file: extract frames, detect faces, draw bounding boxes.
     * 
     * In this simplified implementation:
     * - Frames are SIMULATED as generated images (avoids FFmpeg dependency)
     * - Face detection uses MOCK logic (random but realistic bounding boxes)
     * - Bounding boxes are drawn using Java Graphics2D (real implementation)
     *
     * @param videoPath path to the uploaded video file
     * @param videoId   MongoDB ID of the video document
     * @param outputDir directory to save processed frames
     * @return list of paths to processed frame images
     */
    public List<String> processVideo(String videoPath, String videoId, String outputDir) throws IOException {
        log.info("Processing video: {} (ID: {})", videoPath, videoId);

        List<String> processedFramePaths = new ArrayList<>();
        Random random = new Random(videoId.hashCode()); // Deterministic for same video

        for (int frameNum = 0; frameNum < SIMULATED_FRAME_COUNT; frameNum++) {
            // Step 1: Generate a simulated frame (in production, this would extract from
            // video)
            BufferedImage frame = generateSimulatedFrame(frameNum, random);

            // Step 2: Detect face in the frame (mock detection)
            ROI roi = detectFace(videoId, frameNum, random);

            // Step 3: Draw bounding box on the frame using Graphics2D
            drawBoundingBox(frame, roi);

            // Step 4: Save the processed frame to disk
            String framePath = outputDir + File.separator + "frame_" + frameNum + ".png";
            ImageIO.write(frame, "png", new File(framePath));
            processedFramePaths.add(framePath);

            // Step 5: Save ROI data to MongoDB
            roiRepository.save(roi);

            log.debug("Frame {} processed — ROI: ({}, {}, {}, {})",
                    frameNum, roi.getX(), roi.getY(), roi.getWidth(), roi.getHeight());
        }

        log.info("All {} frames processed for video {}", SIMULATED_FRAME_COUNT, videoId);
        return processedFramePaths;
    }

    /**
     * Generate a simulated video frame.
     * Creates a gradient background with a "person silhouette" circle
     * to make the face detection visualization more meaningful.
     * 
     * In a real system, this would use FFmpeg or JavaCV to extract actual frames.
     */
    private BufferedImage generateSimulatedFrame(int frameNumber, Random random) {
        BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = frame.createGraphics();

        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a gradient background (simulates a scene)
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(40, 40, 60),
                FRAME_WIDTH, FRAME_HEIGHT, new Color(80, 80, 120));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);

        // Draw a simulated "head" (oval) where the face would be detected
        int headX = 540 + (frameNumber * 15); // Slight movement between frames
        int headY = 200;
        int headW = 200;
        int headH = 240;

        // Body shape
        g2d.setColor(new Color(200, 170, 140)); // Skin tone
        g2d.fillOval(headX, headY, headW, headH);

        // Shoulders
        g2d.setColor(new Color(60, 60, 100));
        g2d.fillOval(headX - 80, headY + 200, headW + 160, 200);

        // Frame number label
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Frame #" + frameNumber, 20, 40);

        g2d.dispose();
        return frame;
    }

    /**
     * Mock face detection logic.
     * Generates a realistic bounding box around the simulated face position.
     * 
     * In a real system, this would use:
     * - OpenCV's CascadeClassifier (Haar cascades)
     * - dlib's face detector
     * - A deep learning model (MTCNN, RetinaFace, etc.)
     * 
     * The mock values are deterministic per frame for consistency.
     *
     * @param videoId     ID of the parent video
     * @param frameNumber frame index
     * @param random      seeded random for deterministic results
     * @return ROI document with bounding box coordinates
     */
    private ROI detectFace(String videoId, int frameNumber, Random random) {
        // Generate bounding box coordinates matching the simulated head position
        // Add slight random variation to simulate detection jitter
        int baseX = 530 + (frameNumber * 15);
        int baseY = 190;

        int x = baseX + random.nextInt(20) - 10; // Small jitter
        int y = baseY + random.nextInt(20) - 10;
        int width = 210 + random.nextInt(20) - 10; // ~200px wide
        int height = 250 + random.nextInt(20) - 10; // ~240px tall
        double confidence = 0.85 + (random.nextDouble() * 0.14); // 0.85-0.99

        return ROI.builder()
                .videoId(videoId)
                .frameNumber(frameNumber)
                .x(x)
                .y(y)
                .width(width)
                .height(height)
                .confidence(Math.round(confidence * 100.0) / 100.0) // Round to 2 decimal places
                .label("face")
                .build();
    }

    /**
     * Draw a bounding box rectangle on a frame using Java Graphics2D.
     * This is the REAL drawing logic — NOT mock, NOT OpenCV.
     * 
     * Uses:
     * - Green rectangle border (standard for face detection)
     * - Semi-transparent fill for visibility
     * - Label with confidence score
     * - Anti-aliased rendering
     *
     * @param frame the BufferedImage to draw on
     * @param roi   the ROI containing bounding box coordinates
     */
    private void drawBoundingBox(BufferedImage frame, ROI roi) {
        Graphics2D g2d = frame.createGraphics();

        // Enable anti-aliasing for clean lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Semi-transparent green fill inside the bounding box
        g2d.setColor(new Color(0, 255, 0, 30)); // Very transparent green
        g2d.fillRect(roi.getX(), roi.getY(), roi.getWidth(), roi.getHeight());

        // Green border rectangle — standard face detection visualization
        g2d.setColor(new Color(0, 255, 0)); // Bright green
        g2d.setStroke(new BasicStroke(3.0f)); // 3px line width
        g2d.drawRect(roi.getX(), roi.getY(), roi.getWidth(), roi.getHeight());

        // Draw label with confidence score above the bounding box
        String label = String.format("%s (%.0f%%)", roi.getLabel(), roi.getConfidence() * 100);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        // Background for label text (for readability)
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label) + 10;
        int labelHeight = fm.getHeight() + 4;
        g2d.setColor(new Color(0, 255, 0)); // Solid green background
        g2d.fillRect(roi.getX(), roi.getY() - labelHeight, labelWidth, labelHeight);

        // Label text in black
        g2d.setColor(Color.BLACK);
        g2d.drawString(label, roi.getX() + 5, roi.getY() - 5);

        g2d.dispose();
    }
}
