package com.example.books_api.mapper;

import com.example.books_api.dto.book.BookDto;
import com.example.books_api.dto.book.BookResponseDto;
import com.example.books_api.dto.book.UpdateBookDto;
import com.example.books_api.entities.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(Book book);
    // This updates the existing Book entity with data from BookDto


    @Mapping(source = "bookFile.fileName", target = "fileName")
    BookResponseDto toResponseDto(Book entity);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookFromDto(UpdateBookDto dto, @MappingTarget Book entity);
}
