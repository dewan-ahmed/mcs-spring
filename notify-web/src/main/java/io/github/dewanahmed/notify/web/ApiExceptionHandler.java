package io.github.dewanahmed.notify.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation failed");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is not valid JSON");
        problem.setTitle("Bad request");
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> status(ResponseStatusException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(exception.getStatusCode());
        problem.setTitle(exception.getReason() == null ? exception.getStatusCode().toString() : exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(problem);
    }
}
