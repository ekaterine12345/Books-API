package com.example.books_api.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@Data
public final class ApiResponse {
    private Map<String, Object> data = new HashMap<>();
    private Map<String, Object> error = new HashMap<>();

    public ApiResponse(String key, Object value) { // Adding data
        this.data.put(key, value);
    }

    public ApiResponse addData(String key, Object value){
        this.data.put(key, value);
        return this;
    }

    public ApiResponse addError(String key, Object value){
        this.error.put(key, value);
        return this;
    }
}
