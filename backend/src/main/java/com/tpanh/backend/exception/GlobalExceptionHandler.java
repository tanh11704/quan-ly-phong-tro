package com.tpanh.backend.exception;

import com.tpanh.backend.dto.ApiResponse;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

        // Capture to Sentry với context
        Sentry.configureScope(
                scope -> {
                    scope.setTag("errorCode", errorCode.name());
                    scope.setTag("errorCodeNumber", String.valueOf(errorCode.getCode()));
                    scope.setTag("errorMessage", errorCode.getMessage());
                    setUserContext(scope);
                });
        Sentry.captureException(exception);

        log.warn("AppException: {} - {}", errorCode.name(), errorCode.getMessage(), exception);

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        // Trả về 404 cho các lỗi NOT_FOUND
        if (errorCode == ErrorCode.BUILDING_NOT_FOUND
                || errorCode == ErrorCode.ROOM_NOT_FOUND
                || errorCode == ErrorCode.TENANT_NOT_FOUND
                || errorCode == ErrorCode.USER_NOT_FOUND) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(apiResponse);
        }

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(
            final MethodArgumentNotValidException exception) {
        final ErrorCode errorCode = extractErrorCodeFromValidation(exception);
        captureValidationErrorToSentry(exception, errorCode);

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

    private void captureValidationErrorToSentry(
            final MethodArgumentNotValidException exception, final ErrorCode errorCode) {
        Sentry.configureScope(
                scope -> {
                    scope.setTag("validationError", "true");
                    scope.setTag("errorCode", errorCode.name());
                    scope.setTag("errorCodeNumber", String.valueOf(errorCode.getCode()));
                    scope.setTag("validationField", exception.getFieldError().getField());
                    scope.setTag(
                            "rejectedValue",
                            exception.getFieldError().getRejectedValue() != null
                                    ? exception.getFieldError().getRejectedValue().toString()
                                    : "null");
                    setUserContext(scope);
                });
        Sentry.captureException(exception);
    }

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(final RuntimeException exception) {
        // Capture unexpected runtime exceptions
        Sentry.configureScope(
                scope -> {
                    scope.setTag("exceptionType", "RuntimeException");
                    scope.setTag("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION.name());
                    setUserContext(scope);
                });
        Sentry.captureException(exception);

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
        // Capture unexpected exceptions
        Sentry.configureScope(
                scope -> {
                    scope.setTag("exceptionType", exception.getClass().getSimpleName());
                    scope.setTag("errorCode", ErrorCode.UNCATEGORIZED_EXCEPTION.name());
                    scope.setLevel(io.sentry.SentryLevel.ERROR);
                    setUserContext(scope);
                });
        Sentry.captureException(exception);

        log.error("Unexpected Exception occurred", exception);

        final ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(
                exception.getMessage() != null
                        ? exception.getMessage()
                        : ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.internalServerError().body(apiResponse);
    }

    private void setUserContext(final io.sentry.IScope scope) {
        try {
            final var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                final var user = new io.sentry.protocol.User();
                user.setId(authentication.getName());
                scope.setUser(user);
            }
        } catch (final Exception e) {
            // Ignore nếu không lấy được user context
            log.debug("Could not set user context for Sentry", e);
        }
    }
}
