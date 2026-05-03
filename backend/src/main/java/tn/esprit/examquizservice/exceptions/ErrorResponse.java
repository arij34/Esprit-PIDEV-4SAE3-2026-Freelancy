package tn.esprit.examquizservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response format for all exceptions.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private Integer status;
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private String details;
}
