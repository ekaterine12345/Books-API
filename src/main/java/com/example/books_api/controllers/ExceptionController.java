package com.example.books_api.controllers;


import com.example.books_api.dtos.ApiResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionController extends RuntimeException{

//    @ResponseBody
//    @ExceptionHandler(NoSuchElementFoundException.class)
//    public ApiResponse handelUnexpectedException(NoSuchElementFoundException e){
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.addError("error", e.getMessage());
//        return apiResponse;
//    }
//
//    @ResponseBody
//    @ExceptionHandler(IncorrectParameterException.class)
//    public ApiResponse handleIncorrectParameterException(IncorrectParameterException e) {
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.addError("error", e.getMessage());
//        return apiResponse;
//    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ApiResponse handelUnexpectedException(Exception e){
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.addError("unknown ex = ", e.getMessage());
        return apiResponse;
    }
}
