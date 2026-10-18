package com.example.student.n4.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Định dạng response theo đặc tả:
 *  - 1 đối tượng : { "data": {...} }
 *  - danh sách   : { "data": [...], "pagination": { "page", "limit", "total" } }
 *  - thông báo   : { "message": "..." }
 * Trường null sẽ không xuất hiện trong JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(T data, Pagination pagination, String message) {

    public record Pagination(int page, int limit, long total) {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null, null);
    }

    public static <T> ApiResponse<List<T>> paged(List<T> data, int page, int limit, long total) {
        return new ApiResponse<>(data, new Pagination(page, limit, total), null);
    }

    public static ApiResponse<Void> msg(String message) {
        return new ApiResponse<>(null, null, message);
    }
}
