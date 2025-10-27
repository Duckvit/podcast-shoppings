package com.mobile.prm392.midleware;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class ValidationHandler {

    //dinh nghia cho moi khi chay gap exception nao do
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidation(MethodArgumentNotValidException exception){
        String message = "";

        // vi loi 400 do ng dung nhap co the nhap sai nhieu thuoc tinh nen phai duyet tung thuoc tinh
        for(FieldError error :  exception.getBindingResult().getFieldErrors()){
            message += error.getField() + ":" + error.getDefaultMessage();
        }

        return new ResponseEntity(message, HttpStatus.BAD_REQUEST);
    }

    // 400 - Bad Request (Duplicate)
    @ExceptionHandler(Duplicate.class)
    public ResponseEntity handleValidation(Duplicate exception){
        return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 401 - Unauthorized
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(SecurityException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 403 - Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 404 - Not Found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 405 - Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 409 - Conflict (ví dụ: duplicate key)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.CONFLICT);
    }

    // 415 - Unsupported Media Type
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(org.springframework.web.HttpMediaTypeNotSupportedException exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // 500 - Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleServerError(Exception exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
