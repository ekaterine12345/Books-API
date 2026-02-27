package com.example.books_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadDto {
    private Resource resource;
    private String fileName;
    private String  contentType;
}
