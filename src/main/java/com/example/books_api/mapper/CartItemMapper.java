package com.example.books_api.mapper;

import com.example.books_api.dtos.cartItem.CartItemResponseDto;
import com.example.books_api.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.title", target = "title")
    @Mapping(source = "book.author", target = "author")
    @Mapping(source = "book.price", target = "price")
    CartItemResponseDto toResponseDto(CartItem entity);
}
