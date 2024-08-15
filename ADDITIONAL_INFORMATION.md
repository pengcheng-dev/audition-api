
# Additional Information

## Overview

This application is a RESTful API that allows users to query audition posts and comments. The application is built using Spring Boot, with various features including logging, metrics collection, and tracing integrated into the application.

Key components include:

- **Controllers**: Handle HTTP requests for retrieving posts and comments.
- **Service Layer**: Contains the business logic for interacting with the external API.
- **Integration Layer**: Manages communication with external services.
- **Exception Handling**: Provides a centralized mechanism for handling exceptions across the application.
- **Injector Components**: Provides a centralized mechanism for logging request & response info, collecting metrics, and propagating tracing info across the application.

## Design Decisions

### Logging
- Logging is handled using SLF4J with Logback. Custom loggers are created to standardize the logging format across the application.
- The `RequestLoggingInjector` logs incoming HTTP requests and responses, while the `AuditionLogger` abstracts common logging operations.

### Metrics
- Micrometer is used for collecting metrics. The application tracks the number of requests, processing times, and the number of errors.
- Metrics are recorded using `Counter` and `Timer` objects from the `MeterRegistry`, which are initialized in the `ResponseMetricsInjector` interceptor.

### Tracing
- OpenTelemetry is integrated to handle distributed tracing. The `ResponseHeaderInjector` ensures trace IDs and span IDs are injected into HTTP response headers, enabling traceability across service boundaries.

### Exception Handling
- Global exception handling is provided using `@ControllerAdvice` and `ProblemDetail` to standardize error responses. Custom exceptions like `SystemException` and `HttpClientErrorException` are used to handle and represent specific error scenarios.

## How to Run the Application

1. **Install Dependencies**: Ensure Java 17 and Gradle 7/8 are installed.
2. **Build the Project**: Run `./gradlew build` to compile the code and run tests.
3. **Run the Application**: Use `./gradlew bootRun` to start the application.
4. **Access the API**: The API is accessible at `http://localhost:8080`.

## Usage Examples

- **Get All Posts**: `GET /posts` - Retrieves a list of posts.
- **Get a Post by ID**: `GET /posts/{id}` - Retrieves a post by its ID.
- **Get Comments for a Post**: `GET /posts/{id}/comments` - Retrieves comments associated with a specific post.

### Example Request

```sh
curl -X GET "http://localhost:8080/posts" -H "accept: application/json"
```

### Example Response

```json
[
  {
    "userId": 1,
    "id": 1,
    "title": "Post Title",
    "body": "Post content here"
  }
]
```

## Testing Strategy

### Unit Tests

- Unit tests are provided for each layer of the application, ensuring that the service, integration, and controller layers function as expected.
- Mocks are used to isolate components during testing, such as mocking external API calls and metrics recording, and trace spanning.
  
### Key Tests

- **AuditionServiceTest**: Tests the business logic for retrieving posts and comments.
- **ResponseMetricsInjectorTest**: Validates that metrics are correctly recorded for each request.
- **ExceptionControllerAdviceTest**: Ensures that the exception handling logic produces the correct `ProblemDetail` responses.

## Known Issues or Limitations

- The application currently relies on a fixed external API (https://jsonplaceholder.typicode.com) for retrieving posts and comments. This could be made configurable for better flexibility.
- Libraries of OpenTelemetry are using alpha versions, which may contain hidden bugs.
- Tracing info needs to be exported to other tools like Jaeger.
- Request info logged is not comprehensive, and the response info logged may include sensitive data, introducing a security risk.
- CORS Issues may happen if calling from cross-origin.

## Future Enhancements

- **Pagination**: Add pagination support to the `/posts` endpoint to handle large datasets.
- **Security**: Implement authentication and authorization to protect the API endpoints.
- **Caching**: Introduce caching mechanisms to reduce latency and improve performance.
- **Rate Limiting & Circuit Breaker**: Introduce rate limiting and circuit breaker mechanisms to further protect the service.
- **Service Registry & Discovery**: Introduce service registry and discovery tools for this service.
- **API Gateway**: Introduce an API gateway to provide service facade, routing, and protection.

## Security Considerations

- Ensure all inputs are validated to prevent injection attacks.
- Implement HTTPS for secure communication, especially when deploying to production.
- Implement resource-based authorization.

## Performance Considerations

- The current implementation is suitable for moderate traffic. For high-traffic scenarios, consider adding load balancing and scaling out the application.
- Caching frequently accessed data (like posts and comments) could significantly improve response times.

## API Documentation

- The API adheres to REST principles and returns responses in JSON format.
- Consider adding Swagger documentation to make the API easier to understand and consume.