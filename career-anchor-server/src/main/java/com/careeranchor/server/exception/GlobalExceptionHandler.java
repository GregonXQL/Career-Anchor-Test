package com.careeranchor.server.exception;

import com.careeranchor.server.dto.ApiResponse;
import com.careeranchor.server.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    ResponseEntity<ApiResponse<Void>> handleBiz(BizException exception) {
        ErrorCode error = exception.errorCode();
        return ResponseEntity.status(error.status()).body(ApiResponse.error(error.code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return validationResponse(message);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        return validationResponse(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unhandled server error", exception);
        ErrorCode error = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(error.status()).body(ApiResponse.error(error.code(), error.message()));
    }

    private ResponseEntity<ApiResponse<Void>> validationResponse(String message) {
        ErrorCode error = ErrorCode.VALIDATION_FAILED;
        return ResponseEntity.status(error.status()).body(ApiResponse.error(error.code(), message));
    }
}
