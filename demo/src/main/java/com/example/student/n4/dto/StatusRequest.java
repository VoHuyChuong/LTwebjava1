package com.example.student.n4.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Body cho PATCH /{id}/status : { "is_active": 1 } hoặc { "is_active": 0 } */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record StatusRequest(
        @NotNull(message = "is_active là bắt buộc")
        @Min(value = 0, message = "is_active chỉ nhận 0 hoặc 1")
        @Max(value = 1, message = "is_active chỉ nhận 0 hoặc 1")
        Integer isActive
) {}
