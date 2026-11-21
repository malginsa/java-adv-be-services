package com.epam.task3.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("foreign_book")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeignBook {

    @Id
    private Long id;

    @Column("isbn")
    private String isbn;

    @Column("title")
    private String title;

    @Column("author")
    private String author;

    @Column("publisher_name")
    private String publisherName;

    @Column("item_price")
    private Integer itemPrice;

    @Column("item_url")
    private String itemUrl;
}
