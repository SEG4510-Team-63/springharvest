package dev.springharvest.library.domains.books.integration.utils.factories;

import dev.springharvest.library.config.TestComponentScanningConfig;
import dev.springharvest.library.domains.authors.integration.utils.clients.AuthorCrudClient;
import dev.springharvest.library.domains.authors.integration.utils.factories.AuthorModelFactory;
import dev.springharvest.library.domains.books.models.dtos.BookDTO;
import dev.springharvest.library.domains.publishers.integration.utils.clients.PublisherCrudClient;
import dev.springharvest.library.domains.publishers.integration.utils.factories.PublisherModelFactory;
import dev.springharvest.testing.domains.integration.shared.factories.AbstractModelFactory;
import dev.springharvest.testing.domains.integration.shared.factories.IPKModelFactory;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import(value = {TestComponentScanningConfig.class})
public class BookModelFactory extends AbstractModelFactory<BookDTO, UUID>
    implements IPKModelFactory<BookDTO, UUID> {

  private final AuthorCrudClient authorCrudClient;
  private final AuthorModelFactory authorsModelFactory;
  private final PublisherCrudClient publisherCrudClient;
  private final PublisherModelFactory publishersModelFactory;

  @Autowired(required = true)
  protected BookModelFactory(AuthorCrudClient authorCrudClient, AuthorModelFactory authorsModelFactory, PublisherCrudClient publisherCrudClient,
                             PublisherModelFactory publishersModelFactory) {
    this.authorCrudClient = authorCrudClient;
    this.authorsModelFactory = authorsModelFactory;
    this.publisherCrudClient = publisherCrudClient;
    this.publishersModelFactory = publishersModelFactory;
  }

  @Override
  public UUID getRandomId() {
    return UUID.randomUUID();
  }

  @Override
  public BookDTO buildValidDto() {
    return BookDTO.builder()
        .id(getRandomId())
        .title(RandomStringUtils.randomAlphabetic(5))
        .author(authorCrudClient.createAndExtract(authorsModelFactory.buildValidDto()))
        .publisher(publisherCrudClient.createAndExtract(publishersModelFactory.buildValidDto()))
        .build();
  }


  @Override
  public BookDTO buildValidUpdatedDto(UUID id) {
    BookDTO dto = buildValidDto();
    dto.setId(id);
    return dto;
  }

  @Override
  public BookDTO buildValidUpdatedDto(BookDTO dto) {
    dto.setTitle(RandomStringUtils.randomAlphabetic(5));
    return dto;
  }

  @Override
  public BookDTO buildInvalidDto() {
    return BookDTO.builder().build();
  }

}
