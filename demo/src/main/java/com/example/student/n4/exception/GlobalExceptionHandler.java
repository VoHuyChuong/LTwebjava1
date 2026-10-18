package com.example.student.n4.exception;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Chuyển exception thành JSON { "message": ..., "errors": {...} }.
 * LƯU Ý: class này dùng chung cho cả project. Cả nhóm chỉ nên giữ MỘT bản,
 * nếu bạn khác đã tạo thì dùng bản đó, đừng tạo trùng.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("message", ex.getMessage()));
    }

    /** Lỗi @Valid ở body -> 422, liệt kê từng trường sai (tên trường dạng snake_case). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.putIfAbsent(toSnake(e.getField()), e.getDefaultMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Dữ liệu không hợp lệ");
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /** JSON sai cú pháp, sai kiểu (ví dụ ngày sai định dạng), id không phải UUID -> 400. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Yêu cầu không hợp lệ: sai định dạng JSON, UUID hoặc kiểu dữ liệu"));
    }

    /** Lớp bảo vệ cuối: vi phạm UNIQUE/FK ở CSDL -> 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Dữ liệu vi phạm ràng buộc (ví dụ trùng mã hoặc khóa ngoại không hợp lệ)"));
    }

    /**
     * Lỗi CSDL khác (thiếu bảng, thiếu cột, sai quyền...) -> 500 kèm nguyên nhân thật,
     * để thấy ngay trong Swagger / giao diện thay vì "Error 500" trống.
     * Spring luôn chọn handler cụ thể hơn, nên lỗi trùng mã vẫn ra 409 ở handler phía trên.
     * KHÔNG thêm handler cho Exception chung vì sẽ làm hỏng /v3/api-docs của Swagger.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDb(DataAccessException ex) {
        String cause = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Lỗi cơ sở dữ liệu: " + cause));
    }

    private static String toSnake(String camel) {
        return camel.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}