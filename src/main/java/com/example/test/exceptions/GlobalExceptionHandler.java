package com.example.test.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    // 400 — DTO validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation error");
        problem.setDetail("Invalid request data");
        problem.setProperty("fields", fields);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");

        String detailMessage = "Invalid request format or type mismatch.";

        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                detailMessage = String.format("Invalid value '%s' for field '%s'. Must be one of: %s",
                        ifx.getValue(), ifx.getPath().get(ifx.getPath().size()-1).getFieldName(),
                        java.util.Arrays.toString(ifx.getTargetType().getEnumConstants()));
            }
        }

        problem.setDetail(detailMessage);

        return ResponseEntity.badRequest().body(problem);
    }

    // 401 — invalid login
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(RuntimeException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    // 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access Denied");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid Argument");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.badRequest().body(pd);
    }


    // 423  LOCKED

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ProblemDetail> handleDisabled(DisabledException ex) {

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Account disabled");
        pd.setDetail("Your account has been deactivated");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
    }

    // 409 — already exists
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleConflict(RuntimeException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Conflict");
        problem.setDetail(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(ApplicationAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleApplicationConflict(ApplicationAlreadyExistsException ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Conflict");
        problem.setDetail(ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    // 500 — fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnknown(Exception ex) {

        log.error("Unexpected error", ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("Unexpected server error");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation error");

        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        problem.setDetail("Invalid request parameters");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Business Logic Error");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.badRequest().body(pd);
    }

}
