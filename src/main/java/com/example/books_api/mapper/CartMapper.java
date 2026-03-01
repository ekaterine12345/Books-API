package com.example.books_api.mapper;

import com.example.books_api.dtos.CartResponseDto;
import com.example.books_api.entities.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

    @Mapping(source = "items", target = "items")
    @Mapping(target = "totalPrice", expression = "java(calculateTotal(cart))")
    CartResponseDto toResponseDto(Cart cart);

    default double calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getBook().getPrice())
                .reduce(0.0, Double::sum);
    }
}