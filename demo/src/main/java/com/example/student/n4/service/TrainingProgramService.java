package com.example.student.n4.service;

import com.example.student.n4.dto.TrainingProgramRequest;
import com.example.student.n4.entity.TrainingProgram;
import com.example.student.n4.exception.ApiException;
import com.example.student.n4.repository.TrainingProgramRepository;
import com.example.student.n4.repository.TrainingProgramSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TrainingProgramService {

    private static final Set<String> STATUSES = Set.of("đang đào tạo", "dừng đào tạo");

    private final TrainingProgramRepository repo;
    // Dùng JdbcTemplate để kiểm tra majors / departments / training_program_courses
    // mà KHÔNG phụ thuộc entity của các bạn khác trong nhóm.
    private final JdbcTemplate jdbc;

    public TrainingProgramService(TrainingProgramRepository repo, JdbcTemplate jdbc) {
        this.repo = repo;
        this.jdbc = jdbc;
    }

    // ============ 1. Danh sách ============
    @Transactional(readOnly = true)
    public Page<TrainingProgram> search(String keyword, UUID majorId, UUID departmentId,
                                        String degreeLevel, String educationType, String status,
                                        Integer admissionYear, Boolean isActive, int page, int limit) {
        int p = Math.max(page, 1);
        int l = Math.min(Math.max(limit, 1), 100);
        return repo.findAll(
                TrainingProgramSpecs.filter(keyword, majorId, departmentId, degreeLevel,
                        educationType, status, admissionYear, isActive),
                PageRequest.of(p - 1, l, Sort.by("code")));
    }

    // ============ 2. Chi tiết ============
    @Transactional(readOnly = true)
    public TrainingProgram getById(UUID id) {
        return findActive(id);
    }

    // ============ 3. Thêm mới ============
    @Transactional
    public TrainingProgram create(TrainingProgramRequest req, UUID userId) {
        validate(req, null);
        TrainingProgram tp = new TrainingProgram();
        apply(tp, req);
        tp.setCreatedAt(LocalDateTime.now());
        tp.setCreatedBy(userId);
        tp.setIsActive(true);
        return repo.save(tp);
    }

    // ============ 4. Cập nhật ============
    @Transactional
    public TrainingProgram update(UUID id, TrainingProgramRequest req, UUID userId) {
        TrainingProgram tp = findActive(id);
        validate(req, id);
        apply(tp, req);
        tp.setUpdatedAt(LocalDateTime.now());
        tp.setUpdatedBy(userId);
        return repo.save(tp);
    }

    // ============ 5. Khóa / mở ============
    @Transactional
    public void changeStatus(UUID id, int isActive, UUID userId) {
        TrainingProgram tp = findActive(id);
        tp.setIsActive(isActive == 1);
        tp.setUpdatedAt(LocalDateTime.now());
        tp.setUpdatedBy(userId);
        repo.save(tp);
    }

    // ============ 6. Xóa mềm ============
    @Transactional
    public void delete(UUID id, UUID userId) {
        TrainingProgram tp = findActive(id);
        if (countActiveCourses(id) > 0) {
            throw ApiException.conflict("Chương trình còn học phần đang hoạt động, không thể xóa");
        }
        tp.setDeletedAt(LocalDateTime.now());
        tp.setDeletedBy(userId);
        repo.save(tp);
    }

    // ============ 7. Khôi phục ============
    @Transactional
    public TrainingProgram restore(UUID id, UUID userId) {
        TrainingProgram tp = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy chương trình đào tạo: " + id));
        if (tp.getDeletedAt() == null) {
            throw ApiException.conflict("Chương trình này chưa bị xóa nên không cần khôi phục");
        }
        if (repo.existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(tp.getCode(), id)) {
            throw ApiException.conflict("Mã chương trình đã được dùng bởi bản ghi khác: " + tp.getCode());
        }
        tp.setDeletedAt(null);
        tp.setDeletedBy(null);
        tp.setUpdatedAt(LocalDateTime.now());
        tp.setUpdatedBy(userId);
        return repo.save(tp);
    }

    // ============ 8. Học phần của chương trình ============
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listCourses(UUID id, Boolean isRequired, String groupCode, UUID semesterId) {
        findActive(id); // 404 nếu chương trình không tồn tại
        if (!tableExists("training_program_courses") || !tableExists("courses")) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bảng training_program_courses / courses chưa được tạo (do bạn khác phụ trách).");
        }

        // CHÚ Ý: tên cột của bảng courses (course_code, course_name) lấy theo tài liệu API.
        // Nếu bạn phụ trách courses đặt khác (ví dụ code, name) thì sửa 2 chỗ trong câu SQL này.
        StringBuilder sql = new StringBuilder("""
                SELECT tpc.id, tpc.course_id, c.course_code, c.course_name,
                       tpc.semester_id, tpc.is_required, tpc.group_code, tpc.credits,
                       tpc.is_prerequisite_required, tpc.note, tpc.sort_order, tpc.status
                FROM training_program_courses tpc
                JOIN courses c ON c.id = tpc.course_id
                WHERE tpc.training_program_id = ? AND tpc.deleted_at IS NULL
                """);
        List<Object> params = new ArrayList<>();
        params.add(id.toString());
        if (isRequired != null) { sql.append(" AND tpc.is_required = ?"); params.add(isRequired); }
        if (groupCode != null && !groupCode.isBlank()) { sql.append(" AND tpc.group_code = ?"); params.add(groupCode.trim()); }
        if (semesterId != null) { sql.append(" AND tpc.semester_id = ?"); params.add(semesterId.toString()); }
        sql.append(" ORDER BY tpc.sort_order");

        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    // ============ Hàm hỗ trợ ============
    private TrainingProgram findActive(UUID id) {
        return repo.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy chương trình đào tạo: " + id));
    }

    /** Kiểm tra dữ liệu: 422 nếu sai, 409 nếu trùng mã. excludeId = id đang sửa (null khi thêm mới). */
    private void validate(TrainingProgramRequest r, UUID excludeId) {
        // Lớp bảo vệ thứ hai (lớp thứ nhất là @Valid ở controller)
        if (r.majorId() == null)      throw ApiException.unprocessable("major_id là bắt buộc");
        if (r.departmentId() == null) throw ApiException.unprocessable("department_id là bắt buộc");
        if (r.code() == null || r.code().isBlank()) throw ApiException.unprocessable("code là bắt buộc");
        if (r.name() == null || r.name().isBlank()) throw ApiException.unprocessable("name là bắt buộc");

        if (!existsRow("majors", r.majorId())) {
            throw ApiException.unprocessable("major_id không tồn tại");
        }
        if (!existsRow("departments", r.departmentId())) {
            throw ApiException.unprocessable("department_id không tồn tại");
        }

        // tổng tín chỉ = bắt buộc + tự chọn + thực tập + khóa luận
        boolean hasParts = r.requiredCredits() != null || r.electiveCredits() != null
                || r.internshipCredits() != null || r.thesisCredits() != null;
        if (r.totalCredits() != null && hasParts) {
            BigDecimal sum = nz(r.requiredCredits()).add(nz(r.electiveCredits()))
                    .add(nz(r.internshipCredits())).add(nz(r.thesisCredits()));
            if (sum.compareTo(r.totalCredits()) != 0) {
                throw ApiException.unprocessable("total_credits (" + r.totalCredits()
                        + ") phải bằng bắt buộc + tự chọn + thực tập + khóa luận (" + sum + ")");
            }
        }

        if (r.durationYears() != null && r.maxDurationYears() != null
                && r.maxDurationYears().compareTo(r.durationYears()) < 0) {
            throw ApiException.unprocessable("max_duration_years không được nhỏ hơn duration_years");
        }
        if (r.effectiveDate() != null && r.expiryDate() != null
                && !r.expiryDate().isAfter(r.effectiveDate())) {
            throw ApiException.unprocessable("expiry_date phải sau effective_date");
        }
        if (r.status() != null && !r.status().isBlank()
                && !STATUSES.contains(r.status().trim().toLowerCase())) {
            throw ApiException.unprocessable("status chỉ nhận: 'Đang đào tạo' hoặc 'Dừng đào tạo'");
        }

        String code = r.code().trim();
        boolean duplicated = (excludeId == null)
                ? repo.existsByCodeIgnoreCaseAndDeletedAtIsNull(code)
                : repo.existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(code, excludeId);
        if (duplicated) {
            throw ApiException.conflict("Mã chương trình đã tồn tại: " + code);
        }
    }

    /** Chép dữ liệu từ request vào entity (PUT thay toàn bộ nên trường không gửi sẽ thành null). */
    private void apply(TrainingProgram tp, TrainingProgramRequest r) {
        tp.setCode(r.code().trim());
        tp.setName(r.name().trim());
        tp.setNameEn(r.nameEn());
        tp.setMajorId(r.majorId());
        tp.setDepartmentId(r.departmentId());
        tp.setDegreeLevel(r.degreeLevel());
        tp.setEducationType(r.educationType());
        tp.setTotalCredits(r.totalCredits());
        tp.setRequiredCredits(r.requiredCredits());
        tp.setElectiveCredits(r.electiveCredits());
        tp.setInternshipCredits(r.internshipCredits());
        tp.setThesisCredits(r.thesisCredits());
        tp.setAdmissionYear(r.admissionYear());
        tp.setDurationYears(r.durationYears());
        tp.setMaxDurationYears(r.maxDurationYears());
        tp.setEffectiveDate(r.effectiveDate());
        tp.setExpiryDate(r.expiryDate());
        tp.setDescription(r.description());
        tp.setObjectives(r.objectives());
        tp.setLearningOutcomes(r.learningOutcomes());
        tp.setVersion(r.version());
        tp.setStatus(r.status());
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** table chỉ nhận hằng số trong code (không lấy từ người dùng) nên an toàn với SQL injection. */
    private boolean existsRow(String table, UUID id) {
        if (!tableExists(table)) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Bảng '" + table
                    + "' chưa tồn tại trong cơ sở dữ liệu. Chạy ứng dụng với n4-init.sql "
                    + "hoặc hỏi bạn phụ trách bảng này.");
        }
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE id = ? AND deleted_at IS NULL",
                Integer.class, id.toString());
        return n != null && n > 0;
    }

    /** Kiểm tra bảng có tồn tại không, không phụ thuộc loại exception mà Spring ném ra. */
    private boolean tableExists(String table) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name = ?", Integer.class, table);
        return n != null && n > 0;
    }

    private int countActiveCourses(UUID programId) {
        // Bảng training_program_courses do bạn khác làm: chưa có thì coi như chương trình chưa có học phần.
        if (!tableExists("training_program_courses")) return 0;
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM training_program_courses "
                        + "WHERE training_program_id = ? AND deleted_at IS NULL AND is_active = 1",
                Integer.class, programId.toString());
        return n == null ? 0 : n;
    }
}