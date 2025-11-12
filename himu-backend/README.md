# Himu Chatbot Backend

Spring Boot backend application for the Himu chatbot that integrates with Google Vertex AI.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Google Cloud SDK (gcloud) installed and authenticated (optional, if using gcloud auth)
- Google Cloud Project with Vertex AI endpoint configured

## Setup

### 1. Configure Environment Variables

Set the following environment variables:

```bash
export VERTEX_AI_PROJECT_ID=your-project-id
export VERTEX_AI_ENDPOINT_ID=your-endpoint-id
export VERTEX_AI_ACCESS_TOKEN=your-access-token  # Optional
```

Alternatively, you can set these in `application.properties` or pass them as system properties.

### 2. Authentication

The application supports three methods for authentication (in order of priority):

1. **Environment Variable**: Set `VERTEX_AI_ACCESS_TOKEN`
2. **Application Properties**: Set `vertex.api.access-token` in `application.properties`
3. **gcloud SDK**: Run `gcloud auth application-default login` or `gcloud auth login`

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### POST /api/chat

Send a message to the Himu chatbot.

**Request Body:**
```json
{
  "message": "who are you?"
}
```

**Response:**
```json
{
  "reply": "আমি হিমু..."
}
```

## Configuration

Edit `src/main/resources/application.properties` to configure:

- Server port (default: 8080)
- Vertex AI project ID
- Vertex AI endpoint ID
- Vertex AI location (default: us-central1)
- Access token (optional)

## Architecture

- **Controller**: `ChatController` - Handles HTTP requests
- **Service**: `VertexAiService` - Integrates with Vertex AI API
- **DTOs**: `ChatRequest`, `ChatResponse` - Data transfer objects

The service automatically prepends the "Master Instruction Prompt" to every user message to ensure the model responds as Himu in Bengali.

## CORS

CORS is enabled for `http://localhost:5173` to allow the React frontend to communicate with the backend.

## Troubleshooting

1. **Authentication Error**: Make sure you have set up authentication using one of the three methods above.
2. **Connection Error**: Verify your Vertex AI endpoint ID and project ID are correct.
3. **Port Already in Use**: Change the server port in `application.properties`.

