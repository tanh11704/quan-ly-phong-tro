package com.tpanh.backend.exception;

import com.tpanh.backend.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@io.swagger.v3.oas.annotations.Hidden
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(final AppException exception) {
        final ErrorCode errorCode = exception.getErrorCode();

        if (errorCode == ErrorCode.INVALID_CREDENTIALS) {
            log.info("Authentication failed: {}", errorCode.getMessage());
        } else {
            log.warn("AppException: {} - {}", errorCode.name(), errorCode.getMessage());
        }

        final ApiResponse<Void> apiResponse = buildErrorResponse(errorCode);
        return buildResponseEntity(errorCode, apiResponse);
    }

    private ApiResponse<Void> buildErrorResponse(final ErrorCode errorCode) {
        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return apiResponse;
    }

    private ResponseEntity<ApiResponse<Void>> buildResponseEntity(
            final ErrorCode errorCode, final ApiResponse<Void> apiResponse) {
        if (isNotFoundError(errorCode)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(apiResponse);
        }
        return ResponseEntity.badRequest().body(apiResponse);
    }

    private boolean isNotFoundError(final ErrorCode errorCode) {
        return errorCode == ErrorCode.BUILDING_NOT_FOUND
                || errorCode == ErrorCode.ROOM_NOT_FOUND
                || errorCode == ErrorCode.TENANT_NOT_FOUND
                || errorCode == ErrorCode.USER_NOT_FOUND;
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAccessDeniedException(
            final AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.FORBIDDEN.getCode());
        apiResponse.setMessage(ErrorCode.FORBIDDEN.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(
            final MethodArgumentNotValidException exception) {
        final ErrorCode errorCode = extractErrorCodeFromValidation(exception);

        log.warn(
                "Validation error: {} - Field: {}",
                errorCode.getMessage(),
                exception.getFieldError().getField());

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handlingHttpMessageNotReadable(
            final HttpMessageNotReadableException exception) {
        log.warn("HTTP message not readable: {}", exception.getMessage());

        String errorMessage = "Dữ liệu không hợp lệ";
        if (exception.getMessage() != null) {
            if (exception.getMessage().contains("WaterCalcMethod")) {
                errorMessage =
                        "Phương pháp tính nước không hợp lệ. Chỉ chấp nhận: BY_METER hoặc PER_CAPITA";
            } else if (exception.getMessage().contains("Enum")) {
                errorMessage = "Giá trị enum không hợp lệ";
            }
        }

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.INVALID_KEY.getCode());
        apiResponse.setMessage(errorMessage);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiResponse<Void>> handlingHttpRequestMethodNotSupported(
            final HttpRequestMethodNotSupportedException exception) {
        log.warn(
                "HTTP method not supported: {} for URL. Supported methods: {}",
                exception.getMethod(),
                exception.getSupportedHttpMethods());

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.INVALID_KEY.getCode());
        apiResponse.setMessage(
                String.format(
                        "Phương thức HTTP '%s' không được hỗ trợ cho endpoint này. "
                                + "Các phương thức được hỗ trợ: %s",
                        exception.getMethod(), exception.getSupportedHttpMethods()));

        return ResponseEntity.status(org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED)
                .body(apiResponse);
    }

    private ErrorCode extractErrorCodeFromValidation(
            final MethodArgumentNotValidException exception) {
        final String enumKey = exception.getFieldError().getDefaultMessage();
        try {
            return ErrorCode.valueOf(enumKey);
        } catch (final IllegalArgumentException e) {
            return ErrorCode.INVALID_KEY;
        }
    }

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(final RuntimeException exception) {
        log.error("Unexpected RuntimeException occurred", exception);

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(
                exception.getMessage() != null
                        ? exception.getMessage()
                        : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handlingException(final Exception exception) {
        log.error("Unexpected Exception occurred", exception);

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(
                exception.getMessage() != null
                        ? exception.getMessage()
                        : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.internalServerError().body(apiResponse);
    }
}
