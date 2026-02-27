package com.example.books_api.entities;

import com.example.books_api.dtos.BookDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "books")
public class Book extends BaseEntity<Long> {
    @Id
    @Column(name = "book_id")
    @EqualsAndHashCode.Include
    @SequenceGenerator(name="bookSeqId", sequenceName = "BOOK_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="bookSeqId")
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;
    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(name = "page_number")
    private Integer pageNumber; // number of pages in a book
    @Column(nullable = false)
    private Double price;
    private String description;

 //   private String bookFileName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_id")
    private BookFile bookFile;

    @ManyToMany(mappedBy = "purchasedBooks")
    @JsonIgnore
    private List<User> buyers = new ArrayList<>();

    public Book(BookDto bookDto) {
        this.title = bookDto.getTitle();
        this.author = bookDto.getAuthor();
        this.publishYear = bookDto.getPublishYear();
        this.pageNumber = bookDto.getPageNumber();
        this.price = bookDto.getPrice();
        this.description = bookDto.getDescription();
    }


    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publishYear=" + publishYear +
                ", pageNumber=" + pageNumber +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }
}
