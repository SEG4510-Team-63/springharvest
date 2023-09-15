package dev.springharvest.library.domains.books.models.entities;


import dev.springharvest.library.domains.authors.models.entities.AuthorEntity;
import dev.springharvest.library.domains.publishers.models.entities.PublisherEntity;
import dev.springhavest.common.models.entities.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "books")
@AttributeOverride(name = "id", column = @Column(name = "id"))
public class BookEntity extends BaseEntity<UUID> {

    @Column(name = "title")
    private String title;

    @JoinColumn(name = "author_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private AuthorEntity author;

    @JoinColumn(name = "publisher_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PublisherEntity publisher;

}