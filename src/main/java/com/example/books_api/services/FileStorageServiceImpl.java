package com.example.books_api.services;

import org.springframework.stereotype.Service;

import com.example.books_api.exceptions.FileNotFoundException;
import com.example.books_api.exceptions.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



@Service
public class FileStorageServiceImpl implements FileStorageService{
    private final String baseUploadDir;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String baseUploadDir) {
        this.baseUploadDir = baseUploadDir;
    }

    @Override
    public String saveFile(Long bookId, MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName.contains("..")) {  // security
            throw new FileStorageException("Invalid file name");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new FileStorageException("Only PDF files are allowed");
        }

      //  String uploadDir = baseUploadDir + bookId;
        Path uploadPath = Paths.get(baseUploadDir, bookId.toString());
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath);

        return filePath.toString();
    }

    @Override
    public Resource loadFileAsResource(String filePath) throws MalformedURLException {
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("File not found or not readable");
        }

        return resource;
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null) return;

        try{
            Files.deleteIfExists(Paths.get(filePath));
        }
        catch (IOException e){
            throw new FileStorageException("Failed to delete file: " + filePath, e);
        }
    }
}
