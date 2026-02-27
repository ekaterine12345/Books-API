package com.example.books_api.controllers;


import com.example.books_api.dtos.ApiResponse;
import com.example.books_api.exceptions.BookAccessDeniedException;
import com.example.books_api.exceptions.FileNotFoundException;
import com.example.books_api.exceptions.FileStorageException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionController{
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        ApiResponse apiResponse = new ApiResponse();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            apiResponse.addError(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(EntityNotFoundException ex) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.addError("entity_not_found", ex.getMessage());

        return ResponseEntity.status(404).body(apiResponse);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse> handleFileNotFound(FileNotFoundException ex) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.addError("file_not_found", ex.getMessage());

        return ResponseEntity.status(404).body(apiResponse);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse> handleFileStorageException(FileStorageException ex) {

        ApiResponse apiresponse = new ApiResponse();
        apiresponse.addError("file_error", ex.getMessage());

        return ResponseEntity.status(500).body(apiresponse);
    }

    @ExceptionHandler(BookAccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleBookAccessDenied(BookAccessDeniedException ex){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.addError("access_denied", ex.getMessage());

        return ResponseEntity.status(403)
                .body(apiResponse);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handelUnexpectedException(Exception e){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.addError("unknown ex = ", e.getMessage());
        return ResponseEntity.status(500).body(apiResponse);
    }
}
