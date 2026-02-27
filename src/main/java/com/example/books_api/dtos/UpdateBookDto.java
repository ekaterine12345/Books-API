package com.example.books_api.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookDto {

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(min = 2, max = 100, message = "Author must be between 2 and 100 characters")
    private String author;

    @Min(value = 1500, message = "Publish year must be realistic")
    @Max(value = 2100, message = "Publish year cannot be in far future")
    private Integer publishYear;

    @Positive(message = "Page number must be positive")
    private Integer pageNumber;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    private String description;
}
