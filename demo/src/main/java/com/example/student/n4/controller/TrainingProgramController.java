package com.example.student.n4.controller;

import com.example.student.n4.common.ApiResponse;
import com.example.student.n4.dto.StatusRequest;
import com.example.student.n4.dto.TrainingProgramRequest;
import com.example.student.n4.entity.TrainingProgram;
import com.example.student.n4.service.TrainingProgramService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/training-programs")
public class TrainingProgramController {

    private static final String USER_HEADER = "X-User-Id";

    private final TrainingProgramService service;

    public TrainingProgramController(TrainingProgramService service) {
        this.service = service;
    }

    // 1. GET danh sách (phân trang + tìm kiếm + lọc)
    @GetMapping
    public ApiResponse<List<TrainingProgram>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "major_id", required = false) UUID majorId,
            @RequestParam(name = "department_id", required = false) UUID departmentId,
            @RequestParam(name = "degree_level", required = false) String degreeLevel,
            @RequestParam(name = "education_type", required = false) String educationType,
            @RequestParam(required = false) String status,
            @RequestParam(name = "admission_year", required = false) Integer admissionYear,
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Page<TrainingProgram> result = service.search(keyword, majorId, departmentId, degreeLevel,
                educationType, status, admissionYear, isActive, page, limit);
        return ApiResponse.paged(result.getContent(), result.getNumber() + 1,
                result.getSize(), result.getTotalElements());
    }

    // 2. GET chi tiết
    @GetMapping("/{id}")
    public ApiResponse<TrainingProgram> getById(@PathVariable UUID id) {
        return ApiResponse.ok(service.getById(id));
    }

    // 3. POST thêm mới -> 201
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TrainingProgram> create(
            @Valid @RequestBody TrainingProgramRequest req,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId) {
        return ApiResponse.ok(service.create(req, userId));
    }

    // 4. PUT cập nhật
    @PutMapping("/{id}")
    public ApiResponse<TrainingProgram> update(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingProgramRequest req,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId) {
        return ApiResponse.ok(service.update(id, req, userId));
    }

    // 5. PATCH khóa / mở
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusRequest req,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId) {
        service.changeStatus(id, req.isActive(), userId);
        return ApiResponse.msg(req.isActive() == 1 ? "Đã mở hoạt động chương trình" : "Đã khóa chương trình");
    }

    // 6. DELETE xóa mềm
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable UUID id,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId) {
        service.delete(id, userId);
        return ApiResponse.msg("Đã xóa chương trình đào tạo");
    }

    // 7. PATCH khôi phục
    @PatchMapping("/{id}/restore")
    public ApiResponse<TrainingProgram> restore(
            @PathVariable UUID id,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId) {
        return ApiResponse.ok(service.restore(id, userId));
    }

    // 8. GET học phần của chương trình
    @GetMapping("/{id}/courses")
    public ApiResponse<List<Map<String, Object>>> courses(
            @PathVariable UUID id,
            @RequestParam(name = "is_required", required = false) Boolean isRequired,
            @RequestParam(name = "group_code", required = false) String groupCode,
            @RequestParam(name = "semester_id", required = false) UUID semesterId) {
        return ApiResponse.ok(service.listCourses(id, isRequired, groupCode, semesterId));
    }
}
