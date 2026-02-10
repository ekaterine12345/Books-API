package com.example.books_api.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private String title;
    private String author;
    private Integer publishYear;
    private Integer pageNumber; // number of pages in a book
    private Double price;
    private String description;
    private String bookFileName;
}
