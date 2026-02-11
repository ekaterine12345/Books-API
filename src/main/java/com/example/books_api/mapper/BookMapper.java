package com.example.books_api.mapper;

import com.example.books_api.dtos.BookDto;
import com.example.books_api.entities.Book;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(Book book);
    // This updates the existing Book entity with data from BookDto
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookFromDto(BookDto dto, @MappingTarget Book entity);
}
