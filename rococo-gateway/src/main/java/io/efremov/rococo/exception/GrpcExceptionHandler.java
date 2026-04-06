package io.efremov.rococo.exception;

import io.grpc.StatusRuntimeException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GrpcExceptionHandler {

  @ExceptionHandler(StatusRuntimeException.class)
  public ResponseEntity<Map<String, String>> handleGrpcException(StatusRuntimeException e) {
    HttpStatus status = switch (e.getStatus().getCode()) {
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case ALREADY_EXISTS -> HttpStatus.CONFLICT;
      case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
      case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
      case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
    log.warn("gRPC exception {}: {}", status, e.getStatus().getDescription());
    return ResponseEntity.status(status)
        .body(Map.of("error", e.getStatus().getDescription() != null
            ? e.getStatus().getDescription()
            : e.getStatus().getCode().name()));
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Bad request"));
  }
}
