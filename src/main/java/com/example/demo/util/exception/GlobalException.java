package com.example.demo.util.exception;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.demo.domain.response.ResResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalException {

    // Handle Lỗi sử dụng @Valid check input
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
            WebRequest request) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());
        res.setTimestamp(new Date());
        res.setPath(request.getDescription(false).replace("uri=", ""));

        String message = ex.getBindingResult().getFieldErrors()
                .stream().map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        res.setMessage(message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResResponse<Object>> handleEnumValidateException(IllegalArgumentException ex,
            WebRequest request) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Validate Enum Error...");
        res.setTimestamp(new Date());
        res.setPath(request.getDescription(false).replace("uri=", ""));
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(InvalidException.class)
    public ResponseEntity<ResResponse<Object>> handleInvalidException(Exception ex, WebRequest request) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Exception occurs...");
        res.setTimestamp(new Date());
        res.setPath(request.getDescription(false).replace("uri=", ""));
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResResponse<Object>> handleConstraintViolationException(Exception ex, WebRequest request) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        res.setTimestamp(new Date());
        res.setPath(request.getDescription(false).replace("uri=", ""));
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ResResponse<Object>> handleStorageException(Exception ex, WebRequest request) {
        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Exception upload file");
        res.setTimestamp(new Date());
        res.setPath(request.getDescription(false).replace("uri=", ""));
        res.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

}
