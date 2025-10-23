package io.ganeshannt.asm.ops.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized error response")
public class ErrorResponseDTO {

    /**
     * ISO 8601 timestamp when error occurred
     * Helps with debugging and log correlation
     */
    @Schema(description = "When the error occurred", example = "2025-10-22T22:30:00")
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     */
    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    /**
     * Short error description
     */
    @Schema(description = "Error type", example = "Bad Request")
    private String error;

    /**
     * Human-readable error message
     * Should be safe to display to end users
     */
    @Schema(description = "Detailed error message", example = "Product name is required")
    private String message;

    /**
     * Request path that caused the error
     * Helps with debugging
     */
    @Schema(description = "Request path", example = "/api/v1/orders")
    private String path;

    /**
     * Validation errors (if any)
     * Used when multiple fields have validation issues
     */
    @Schema(description = "List of validation errors")
    private List<ValidationError> validationErrors;

    /**
     * Nested class for field-level validation errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field-level validation error")
    public static class ValidationError {

        @Schema(description = "Field name", example = "customerEmail")
        private String field;

        @Schema(description = "Rejected value", example = "invalid-email")
        private String rejectedValue;

        @Schema(description = "Error message", example = "Customer email must be valid")
        private String message;
    }
}
