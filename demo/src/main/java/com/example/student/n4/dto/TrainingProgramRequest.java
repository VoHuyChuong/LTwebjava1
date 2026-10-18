package com.example.student.n4.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Body cho POST và PUT /api/v1/training-programs (dấu * trong đặc tả = @NotNull/@NotBlank). */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TrainingProgramRequest(
        @NotNull(message = "major_id là bắt buộc") UUID majorId,
        @NotNull(message = "department_id là bắt buộc") UUID departmentId,
        @NotBlank(message = "code là bắt buộc") @Size(max = 100) String code,
        @NotBlank(message = "name là bắt buộc") @Size(max = 255) String name,
        @Size(max = 255) String nameEn,
        @Size(max = 50) String degreeLevel,
        @Size(max = 50) String educationType,
        @DecimalMin(value = "0.0", message = "total_credits phải >= 0") BigDecimal totalCredits,
        @DecimalMin(value = "0.0", message = "required_credits phải >= 0") BigDecimal requiredCredits,
        @DecimalMin(value = "0.0", message = "elective_credits phải >= 0") BigDecimal electiveCredits,
        @DecimalMin(value = "0.0", message = "internship_credits phải >= 0") BigDecimal internshipCredits,
        @DecimalMin(value = "0.0", message = "thesis_credits phải >= 0") BigDecimal thesisCredits,
        Integer admissionYear,
        @DecimalMin(value = "0.0", message = "duration_years phải >= 0") BigDecimal durationYears,
        @DecimalMin(value = "0.0", message = "max_duration_years phải >= 0") BigDecimal maxDurationYears,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        String description,
        String objectives,
        String learningOutcomes,
        @Size(max = 20) String version,
        @Size(max = 20) String status
) {}
