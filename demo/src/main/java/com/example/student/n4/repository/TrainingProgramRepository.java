package com.example.student.n4.repository;

import com.example.student.n4.entity.TrainingProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingProgramRepository
        extends JpaRepository<TrainingProgram, UUID>, JpaSpecificationExecutor<TrainingProgram> {

    /** Chỉ lấy bản ghi chưa xóa mềm. */
    Optional<TrainingProgram> findByIdAndDeletedAtIsNull(UUID id);

    /** Kiểm tra trùng mã (khi thêm mới). */
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    /** Kiểm tra trùng mã (khi sửa / khôi phục, bỏ qua chính bản ghi đó). */
    boolean existsByCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(String code, UUID id);
}
