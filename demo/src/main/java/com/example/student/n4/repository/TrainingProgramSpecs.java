package com.example.student.n4.repository;

import com.example.student.n4.entity.TrainingProgram;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Gộp các điều kiện lọc của GET /api/v1/training-programs. Tham số null = bỏ qua. */
public final class TrainingProgramSpecs {

    private TrainingProgramSpecs() {}

    public static Specification<TrainingProgram> filter(
            String keyword, UUID majorId, UUID departmentId, String degreeLevel,
            String educationType, String status, Integer admissionYear, Boolean isActive) {

        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            // luôn loại bản ghi đã xóa mềm
            p.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("nameEn")), like)));
            }
            if (majorId != null)       p.add(cb.equal(root.get("majorId"), majorId));
            if (departmentId != null)  p.add(cb.equal(root.get("departmentId"), departmentId));
            if (degreeLevel != null && !degreeLevel.isBlank())
                p.add(cb.equal(cb.lower(root.get("degreeLevel")), degreeLevel.trim().toLowerCase()));
            if (educationType != null && !educationType.isBlank())
                p.add(cb.equal(cb.lower(root.get("educationType")), educationType.trim().toLowerCase()));
            if (status != null && !status.isBlank())
                p.add(cb.equal(cb.lower(root.get("status")), status.trim().toLowerCase()));
            if (admissionYear != null) p.add(cb.equal(root.get("admissionYear"), admissionYear));
            if (isActive != null)      p.add(cb.equal(root.get("isActive"), isActive));

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
