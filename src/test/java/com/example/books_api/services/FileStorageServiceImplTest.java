package com.example.books_api.services;

import com.example.books_api.exceptions.FileNotFoundException;
import com.example.books_api.exceptions.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;


class FileStorageServiceImplTest {
    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp(){
        fileStorageService = new FileStorageServiceImpl(tempDir.toString());
    }

    // ================================================ saveFile =========================================
    @Test
    void shouldSavePdfFileSuccessfully() throws IOException {
        // Given
        Long bookId = 1L;
        String bookFileName = "book_1.pdf";
        MockMultipartFile file = new MockMultipartFile("file",
                bookFileName, "application/pdf",
                "some content".getBytes());
        // When
        String path = fileStorageService.saveFile(bookId, file);
        Path savedPath = Paths.get(path);

        // Then
        assertThat(Files.exists(savedPath)).isTrue();
        assertThat(Files.isDirectory(savedPath.getParent())).isTrue();

        assertThat(savedPath.getFileName().toString()).isEqualTo(bookFileName);
        assertThat(savedPath.getParent().getFileName().toString())
                .isEqualTo(bookId.toString());
        String content = Files.readString(savedPath);
        assertThat(content).isEqualTo("some content");
    }

    @Test
    void shouldThrowIfFileIsNotPdf(){
        // Given
        Long bookId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "book.pdf",
                "text/plain", "some content".getBytes());

        // When, Then
        assertThatThrownBy(() -> fileStorageService.saveFile(bookId, file))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Only PDF files are allowed");

    }

    @Test
    void shouldThrowIfFileNameContainsPathTraversal(){
        // Given
        Long bookId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "../book.pdf",
                "application/pdf", "some content".getBytes());

        // When, then
        assertThatThrownBy(() -> fileStorageService.saveFile(bookId, file))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Invalid file name");

    }

    // =====================  load File as Resource =========================================================
    @Test
    void  shouldLoadFileAsResource() throws IOException {
        // Given
        String bookFileName = "book.pdf";
        MockMultipartFile file = new MockMultipartFile("file", bookFileName,
                "application/pdf", "some content".getBytes());
        String path = fileStorageService.saveFile(1L, file);

        // When
        Resource resource = fileStorageService.loadFileAsResource(path);

        // Then
        assertThat(resource.exists()).isTrue();
        assertThat(resource).isNotNull();
        assertThat(resource.getFilename()).isEqualTo(bookFileName);
        assertThat(resource.contentLength()).isGreaterThan(0);
    }

    @Test
    void shouldThrowIfFileNotFound(){
        assertThatThrownBy(() -> fileStorageService.loadFileAsResource("not_exists.pdf"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File not found or not readable");
    }

    // =====================  Delete File =========================================================
    @Test
    void shouldDeleteFileSuccessfully() throws IOException {
        // Given
        Long bookId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "book.pdf",
                "application/pdf", "some content".getBytes());
        String path = fileStorageService.saveFile(bookId, file);

        // When
        fileStorageService.deleteFile(path);

        // Then
        assertThat(Files.exists(Paths.get(path))).isFalse();

    }

    @Test
    void  shouldDoNothingWhenPathIsNull(){
        fileStorageService.deleteFile(null);
//        verify()
    }
}