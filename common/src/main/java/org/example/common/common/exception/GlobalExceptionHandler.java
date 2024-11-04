package org.example.common.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.common.common.dto.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // 예외 로그 기록
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ExceptionResponse> handleAuthException(AuthException e, HttpServletRequest request) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ExceptionResponse.from(
                HttpStatus.UNAUTHORIZED, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidRequestException(InvalidRequestException e, HttpServletRequest request) {
          e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.from(
                HttpStatus.BAD_REQUEST, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ExceptionResponse> handleServerException(ServerException e, HttpServletRequest request) {
          e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.from(
                HttpStatus.BAD_REQUEST, e.getMessage(), URI.create(request.getRequestURI())));
    }

    // EntityNotFoundException 처리
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
          ex.printStackTrace();
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InputOutputException.class)
    public ResponseEntity<ExceptionResponse> handleInputOutputException(InputOutputException e, HttpServletRequest request) {
          e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.from(
                HttpStatus.BAD_REQUEST, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(SlackException.class)
    public ResponseEntity<ExceptionResponse> handleSlackException(SlackException e, HttpServletRequest request) {
          e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.from(
                HttpStatus.BAD_REQUEST, e.getMessage(), URI.create(request.getRequestURI())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionResponse.from(
                HttpStatus.BAD_REQUEST, e.getMessage(), URI.create(request.getRequestURI())));
    }
}
