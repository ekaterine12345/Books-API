package com.example.books_api.dtos;

import com.example.books_api.dtos.cartItem.CartItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartResponseDto {
    private Long cartId;
    private List<CartItemResponseDto> items;
    private Double totalPrice;
}

