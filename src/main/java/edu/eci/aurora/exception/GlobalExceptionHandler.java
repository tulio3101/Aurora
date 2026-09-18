package edu.eci.aurora.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TicketNotFoundException.class)
  public ResponseEntity<String> handleTicketNotFound(TicketNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(InvalidCategoryException.class)
  public ResponseEntity<String> handleInvalidCategory(InvalidCategoryException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(InvalidSeverityException.class)
  public ResponseEntity<String> handleInvalidSeverity(InvalidSeverityException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(InvalidTeamException.class)
  public ResponseEntity<String> handleInvalidTeam(InvalidTeamException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(InvalidSourceException.class)
  public ResponseEntity<String> handleInvalidSource(InvalidSourceException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + " " + error.getDefaultMessage())
        .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> handleParamValidation(ConstraintViolationException ex) {
    String message = ex.getConstraintViolations().stream()
        .map(violation -> {
          String path = violation.getPropertyPath().toString();
          String param = path.substring(path.lastIndexOf('.') + 1);
          return param + " " + violation.getMessage();
        })
        .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleUnreadableBody(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getMostSpecificCause();

    if (cause instanceof RuntimeException && cause.getMessage() != null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cause.getMessage());
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cuerpo del request inválido o mal formado");
  }
}
