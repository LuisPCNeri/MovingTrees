package pt.ua.MovingTree.controllers;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pt.ua.MovingTree.dtos.ApiError;
import pt.ua.MovingTree.exceptions.DuplicateUsernameException;
import pt.ua.MovingTree.exceptions.ForbiddenOperationException;
import pt.ua.MovingTree.exceptions.InvalidPagingException;
import pt.ua.MovingTree.exceptions.TeamNotInMunicipalityException;
import pt.ua.MovingTree.exceptions.UserNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return new ApiError("Validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableBody() {
        return ApiError.of("Invalid request body");
    }

    @ExceptionHandler(InvalidPagingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidPaging(InvalidPagingException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidParameter() {
        return ApiError.of("Invalid request parameter");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFound(UserNotFoundException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(TeamNotInMunicipalityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTeamMismatch(TeamNotInMunicipalityException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenOperationException exception) {
        return ApiError.of(exception.getMessage());
    }

    @ExceptionHandler({DuplicateUsernameException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(RuntimeException exception) {
        if (exception instanceof DuplicateUsernameException) {
            return ApiError.of(exception.getMessage());
        }
        return ApiError.of("User conflicts with existing data");
    }
}
