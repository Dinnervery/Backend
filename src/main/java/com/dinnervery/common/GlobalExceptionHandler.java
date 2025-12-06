package com.dinnervery.common;

import com.dinnervery.dto.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.LazyInitializationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage());
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse("CONFLICT", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse("CONFLICT", "이미 존재하는 계정입니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("ValidationException: {}", e.getMessage());
        
        Map<String, String> error = new HashMap<>();
        
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            error.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException: ", e);
        
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "데이터 처리 중 오류가 발생했습니다. 필요한 데이터가 누락되었을 수 있습니다.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ErrorResponse> handleLazyInitializationException(LazyInitializationException e) {
        log.error("LazyInitializationException: ", e);
        
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "데이터 로딩 중 오류가 발생했습니다. 엔티티 관계 설정을 확인해주세요.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
