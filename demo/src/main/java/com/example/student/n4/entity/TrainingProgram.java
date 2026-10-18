package com.example.student.n4.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Chương trình đào tạo. JSON trả về dùng snake_case (major_id, name_en...)
 * theo đặc tả API. @JsonNaming chỉ áp dụng cho class này, không ảnh hưởng Student.
 */
@Entity
@Table(name = "training_programs")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrainingProgram {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "major_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID majorId;

    @Column(name = "department_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID departmentId;

    @Column(name = "degree_level", length = 50)
    private String degreeLevel;

    @Column(name = "education_type", length = 50)
    private String educationType;

    @Column(name = "total_credits", precision = 5, scale = 1)
    private BigDecimal totalCredits;

    @Column(name = "required_credits", precision = 5, scale = 1)
    private BigDecimal requiredCredits;

    @Column(name = "elective_credits", precision = 5, scale = 1)
    private BigDecimal electiveCredits;

    @Column(name = "internship_credits", precision = 5, scale = 1)
    private BigDecimal internshipCredits;

    @Column(name = "thesis_credits", precision = 5, scale = 1)
    private BigDecimal thesisCredits;

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "duration_years", precision = 5, scale = 1)
    private BigDecimal durationYears;

    @Column(name = "max_duration_years", precision = 5, scale = 1)
    private BigDecimal maxDurationYears;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "objectives", columnDefinition = "nvarchar(max)")
    private String objectives;

    @Column(name = "learning_outcomes", columnDefinition = "nvarchar(max)")
    private String learningOutcomes;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "status", length = 20)
    private String status;

    // ---- Cột audit + xóa mềm ----
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false, columnDefinition = "uniqueidentifier")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "uniqueidentifier")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", columnDefinition = "uniqueidentifier")
    private UUID deletedBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    public TrainingProgram() {}

    // ---- Getter / Setter ----
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public UUID getMajorId() { return majorId; }
    public void setMajorId(UUID majorId) { this.majorId = majorId; }

    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }

    public String getDegreeLevel() { return degreeLevel; }
    public void setDegreeLevel(String degreeLevel) { this.degreeLevel = degreeLevel; }

    public String getEducationType() { return educationType; }
    public void setEducationType(String educationType) { this.educationType = educationType; }

    public BigDecimal getTotalCredits() { return totalCredits; }
    public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }

    public BigDecimal getRequiredCredits() { return requiredCredits; }
    public void setRequiredCredits(BigDecimal requiredCredits) { this.requiredCredits = requiredCredits; }

    public BigDecimal getElectiveCredits() { return electiveCredits; }
    public void setElectiveCredits(BigDecimal electiveCredits) { this.electiveCredits = electiveCredits; }

    public BigDecimal getInternshipCredits() { return internshipCredits; }
    public void setInternshipCredits(BigDecimal internshipCredits) { this.internshipCredits = internshipCredits; }

    public BigDecimal getThesisCredits() { return thesisCredits; }
    public void setThesisCredits(BigDecimal thesisCredits) { this.thesisCredits = thesisCredits; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public BigDecimal getDurationYears() { return durationYears; }
    public void setDurationYears(BigDecimal durationYears) { this.durationYears = durationYears; }

    public BigDecimal getMaxDurationYears() { return maxDurationYears; }
    public void setMaxDurationYears(BigDecimal maxDurationYears) { this.maxDurationYears = maxDurationYears; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }

    public String getLearningOutcomes() { return learningOutcomes; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public UUID getDeletedBy() { return deletedBy; }
    public void setDeletedBy(UUID deletedBy) { this.deletedBy = deletedBy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
