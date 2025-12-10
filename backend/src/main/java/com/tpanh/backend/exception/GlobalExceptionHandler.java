package com.tpanh.backend.exception;

import com.tpanh.backend.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

        log.warn("AppException: {} - {}", errorCode.name(), errorCode.getMessage(), exception);

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
