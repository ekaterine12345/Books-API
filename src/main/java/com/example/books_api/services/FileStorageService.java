package com.example.books_api.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

public interface FileStorageService {
    String saveFile(Long BookId, MultipartFile file) throws IOException;

    Resource loadFileAsResource(String filePath) throws MalformedURLException;

    void deleteFile(String filePath);
}
