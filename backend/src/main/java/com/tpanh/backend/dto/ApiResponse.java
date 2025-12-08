package com.tpanh.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private static final int DEFAULT_SUCCESS_CODE = 1000;
    @Builder.Default private int code = DEFAULT_SUCCESS_CODE;

    private String message;
    private T result;
}
