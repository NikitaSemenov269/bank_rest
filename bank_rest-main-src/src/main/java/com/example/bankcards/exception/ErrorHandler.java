package com.example.bankcards.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ChangeSetPersister.NotFoundException e) {
        log.error("404: {}", e.getMessage());
        return buildError(e.getMessage(), "------------------------", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.error("409: {}", e.getMessage());
        return buildError(e.getMessage(), "------------------------", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("409: Дубликат данных");
        return buildError("------------------------",
                "------------------------", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException e) {
        log.error("400: Ошибка валидации");

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatError)
                .collect(Collectors.toList());

        String message = errors.isEmpty() ? "Ошибка валидации" : errors.get(0);

        return ApiError.builder()
                .errors(errors)
                .message(message)
                .reason("Некорректный запрос")
                .status(HttpStatus.BAD_REQUEST.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("400: Неверный тип параметра");
        String message = String.format("Неверный формат параметра: ожидается %s",
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "число");
        return buildError(message, "Некорректный запрос", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException e) {
        log.warn("403: Доступ запрещен");
        return buildError("У вас нет прав для выполнения этой операции",
                "Доступ запрещен", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorized(AuthenticationException e) {
        log.warn("401: Не авторизован");
        return buildError("Требуется авторизация",
                "Не авторизован", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAll(Exception e) {
        log.error("500: Непредвиденная ошибка", e);
        return buildError("Внутренняя ошибка сервера", "Сервер временно недоступен",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiError buildError(String message, String reason, HttpStatus status) {
        return ApiError.builder()
                .message(message)
                .reason(reason)
                .status(status.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    private String formatError(FieldError error) {
        return String.format("Поле: %s. Ошибка: %s. Значение: %s",
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue());
    }
}
