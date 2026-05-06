# Face Detection Video Processing API

A Spring Boot backend that accepts video uploads, processes them into frames, performs face detection (mock/simulated), generates ROI (Region of Interest) bounding boxes, draws rectangles using Java Graphics2D, and stores everything in MongoDB Atlas.

## 🏗️ Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌──────────────┐
│   Client /   │────▶│  VideoController  │────▶│   VideoService    │────▶│  MongoDB     │
│   Frontend   │◀────│  (REST API)       │◀────│                  │◀────│  Atlas       │
└─────────────┘     └──────────────────┘     └────────┬─────────┘     └──────────────┘
                                                       │
                                                       ▼
                                              ┌──────────────────┐
                                              │ FrameProcessing  │
                                              │    Service       │
                                              │ • Frame Extract  │
                                              │ • Face Detection │
                                              │ • Graphics2D Draw│
                                              └──────────────────┘
```

**Clean Architecture Flow:** `Controller → Service → Repository`

| Layer | Responsibility |
|-------|---------------|
| **Controller** | REST endpoints, request validation, response formatting |
| **Service** | Business logic, video processing orchestration |
| **Repository** | MongoDB CRUD operations via Spring Data |

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/video/upload` | Upload a video file for processing |
| `GET` | `/api/video/{id}` | Get video details and status |
| `GET` | `/api/video/{id}/roi` | Get all ROI (face detection) data |
| `GET` | `/api/video/{id}/frames/{n}` | Download a processed frame image |
| `GET` | `/api/video/health` | Health check |

### Example: Upload a Video
```bash
curl -X POST http://localhost:8080/api/video/upload \
  -F "file=@sample_video.mp4"
```

### Example: Get ROI Data
```bash
curl http://localhost:8080/api/video/{videoId}/roi
```

**Response:**
```json
{
  "videoId": "664f1a2b3c4d5e6f7a8b9c0d",
  "totalFrames": 10,
  "totalDetections": 10,
  "rois": [
    {
      "id": "664f1b3c4d5e6f7a8b9c0d1e",
      "videoId": "664f1a2b3c4d5e6f7a8b9c0d",
      "frameNumber": 0,
      "x": 120,
      "y": 80,
      "width": 150,
      "height": 180,
      "confidence": 0.92,
      "label": "face"
    }
  ]
}
```

## 🗄️ MongoDB Document Models

### Video Collection (`videos`)
```json
{
  "_id": "664f1a2b3c4d5e6f7a8b9c0d",
  "fileName": "interview_clip.mp4",
  "originalFileName": "interview_clip.mp4",
  "filePath": "uploads/664f.../interview_clip.mp4",
  "contentType": "video/mp4",
  "fileSize": 5242880,
  "status": "PROCESSED",
  "frameCount": 10,
  "processedFramePaths": [
    "uploads/664f.../frame_0.png",
    "uploads/664f.../frame_1.png"
  ],
  "uploadedAt": "2024-05-23T10:30:00",
  "processedAt": "2024-05-23T10:30:05"
}
```

### ROI Collection (`rois`)
```json
{
  "_id": "664f1b3c4d5e6f7a8b9c0d1e",
  "videoId": "664f1a2b3c4d5e6f7a8b9c0d",
  "frameNumber": 0,
  "x": 120,
  "y": 80,
  "width": 150,
  "height": 180,
  "confidence": 0.92,
  "label": "face"
}
```

### Index Suggestion
```javascript
// Create index on videoId for fast ROI lookups
db.rois.createIndex({ "videoId": 1 })

// This index is automatically created by Spring Data MongoDB
// via the @Indexed annotation on the ROI.videoId field
```

## 🍃 Why MongoDB?

| Reason | Explanation |
|--------|-------------|
| **Flexible Schema** | Video metadata and ROI data have varying structures; MongoDB's document model accommodates this without rigid migrations |
| **Embedded Arrays** | Frame paths stored as arrays within Video documents — natural fit for document DB |
| **Scalability** | MongoDB Atlas provides horizontal scaling, auto-sharding, and global clusters |
| **JSON-Native** | REST APIs return JSON; MongoDB stores BSON — minimal serialization overhead |
| **Cloud-Ready** | MongoDB Atlas eliminates DB ops — ideal for containerized deployments |

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB Atlas account (free tier works)
- Docker (optional)

### Quick Start for Other Developers

```bash
# 1. Clone the repository
git clone <your-repository-url>
cd face

# 2. Set up environment variables
cp .env.example .env
# Edit .env with your MongoDB Atlas URI

# 3. Run the application
./mvnw spring-boot:run
```

### Detailed Setup

#### 1. Configure MongoDB Atlas

1. Create a free cluster at [MongoDB Atlas](https://www.mongodb.com/atlas)
2. Create a database user
3. Whitelist your IP (or `0.0.0.0/0` for development)
4. Get your connection string

#### 2. Set Environment Variables

```bash

# Edit .env with your actual values:
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/facedb?retryWrites=true&w=majority
UPLOAD_DIR=uploads
```

### 3. Run Locally (Maven)

```bash
# Set the environment variable
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/facedb?retryWrites=true&w=majority"

# Build and run
./mvnw spring-boot:run
```

### 4. Run with Docker

```bash
# Set your MongoDB URI in .env file, then:
docker-compose up --build
```

### 5. Open the Frontend

Navigate to `http://localhost:8080` to use the web UI.

## 📁 Project Structure

```
face/
├── src/main/java/com/project/face/
│   ├── FaceApplication.java          # Spring Boot entry point
│   ├── model/
│   │   ├── Video.java                # Video MongoDB document
│   │   └── ROI.java                  # ROI MongoDB document (with @Indexed)
│   ├── repository/
│   │   ├── VideoRepository.java      # CRUD for videos
│   │   └── ROIRepository.java        # CRUD + findByVideoId for ROIs
│   ├── service/
│   │   ├── VideoService.java         # Upload & retrieval orchestration
│   │   └── FrameProcessingService.java  # Frame extraction + face detection + drawing
│   └── controller/
│       ├── VideoController.java      # REST API endpoints
│       └── HealthController.java     # Health check
├── src/main/resources/
│   ├── application.yaml              # Spring Boot config
│   └── static/index.html             # Simple web frontend
├── Dockerfile                        # Multi-stage Docker build
├── docker-compose.yml                # Backend container config
├── .env.example                      # Environment variable template
└── README.md                         # This file
```

## 🔮 Future Improvements

### Phase 1: Real Video Processing
- Integrate **FFmpeg** (via ProcessBuilder) for actual frame extraction
- Support multiple video formats (MP4, AVI, MOV, WebM)
- Add progress tracking with percentage updates

### Phase 2: Python + FastAPI for ML
```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│ Spring   │────▶│ FastAPI      │────▶│  ML Models   │
│ Boot API │◀────│ (Python)     │◀────│  (OpenCV/    │
│          │     │              │     │   dlib/YOLO) │
└──────────┘     └──────────────┘     └──────────────┘
```
- **FastAPI microservice** for face detection using OpenCV/dlib
- Spring Boot calls FastAPI via REST for ML inference
- Keeps Java for API orchestration, Python for ML (best of both worlds)

### Phase 3: Real-Time Processing with WebSockets
- **WebSocket** endpoint for live video streaming
- **STOMP** protocol via Spring WebSocket
- Real-time face detection results pushed to frontend
- Progress updates during processing

### Phase 4: Advanced Features
- Multiple face detection per frame
- Face recognition (identification, not just detection)
- Emotion detection
- Video summarization with key frames
- Cloud storage (AWS S3 / GCP Cloud Storage) for processed files

## 🧪 Testing with cURL

```bash
# 1. Upload a video
curl -X POST http://localhost:8080/api/video/upload \
  -F "file=@test_video.mp4"

# 2. Get video details (use the ID from step 1)
curl http://localhost:8080/api/video/YOUR_VIDEO_ID

# 3. Get ROI data
curl http://localhost:8080/api/video/YOUR_VIDEO_ID/roi

# 4. Download a processed frame
curl http://localhost:8080/api/video/YOUR_VIDEO_ID/frames/0 --output frame_0.png

# 5. Health check
curl http://localhost:8080/api/video/health
```

## 📝 License

This project is for educational/assignment purposes.
