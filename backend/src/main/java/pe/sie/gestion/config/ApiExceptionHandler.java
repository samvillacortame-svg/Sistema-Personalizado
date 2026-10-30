package pe.sie.gestion.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? mensajePorDefecto(status) : exception.getReason();
        return ResponseEntity.status(status).body(new ApiError(message, Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        String message = fieldErrors.values().stream().distinct().collect(Collectors.joining(". "));
        if (message.isBlank()) message = "Revisa los datos ingresados e inténtalo de nuevo.";
        return ResponseEntity.badRequest().body(new ApiError(message, fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .distinct()
                .collect(Collectors.joining(". "));
        if (message.isBlank()) message = "Revisa los datos ingresados e inténtalo de nuevo.";
        return ResponseEntity.badRequest().body(new ApiError(message, Map.of()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody() {
        return ResponseEntity.badRequest().body(new ApiError(
                "Los datos enviados tienen un formato inválido. Revisa el formulario e inténtalo de nuevo.", Map.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                "Ya existe un registro con esos datos. Verifica el documento ingresado.", Map.of()));
    }

    private String mensajePorDefecto(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "No se encontró el registro solicitado.";
            case CONFLICT -> "El registro entra en conflicto con un dato existente.";
            case FORBIDDEN -> "No tienes permisos para realizar esta operación.";
            default -> "No se pudo completar la operación. Inténtalo nuevamente.";
        };
    }

    public record ApiError(String message, Map<String, String> fieldErrors) { }
}
