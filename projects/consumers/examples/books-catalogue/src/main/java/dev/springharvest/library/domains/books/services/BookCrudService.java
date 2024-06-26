package dev.springharvest.library.domains.books.services;

import dev.springharvest.crud.domains.base.services.AbstractCrudService;
import dev.springharvest.library.domains.books.models.entities.BookEntity;
import dev.springharvest.library.domains.books.persistence.IBookCrudRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import  org.springframework.data.domain.Page;
import  org.springframework.data.domain.Pageable;

@Service
public class BookCrudService extends AbstractCrudService<BookEntity, UUID> {

  @Autowired
  protected BookCrudService(IBookCrudRepository baseRepository) {
    super(baseRepository);
  }

  public List<BookEntity> findByAuthorId(UUID id) {
    return ((IBookCrudRepository) crudRepository).findByAuthorId(id);
  }

  public List<BookEntity> findByPublisherId(UUID id) {
    return ((IBookCrudRepository) crudRepository).findByPublisherId(id);
  }

  public Page<BookEntity> findByTitleContaining(String title, Pageable pageable){
    return ((IBookCrudRepository) crudRepository).findByTitleContainingIgnoreCase(title, pageable);
  }

}
