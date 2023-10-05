package dev.springharvest.library.domains.books.integration.utils.clients;

import dev.springharvest.library.config.TestComponentScanningConfig;
import dev.springharvest.library.domains.books.constants.BookConstants;
import dev.springharvest.library.domains.books.models.dtos.BookDTO;
import dev.springharvest.testing.integration.crud.clients.AbstractCrudClientImpl;
import dev.springharvest.testing.integration.crud.clients.uri.CrudUriFactory;
import dev.springharvest.testing.integration.shared.clients.RestClientImpl;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import(value = {TestComponentScanningConfig.class})
public class BooksCrudClient extends AbstractCrudClientImpl<BookDTO, UUID> {

  @Autowired(required = true)
  protected BooksCrudClient(RestClientImpl clientHelper) {
    super(clientHelper, new CrudUriFactory(BookConstants.Controller.DOMAIN_CONTEXT), BookDTO.class);
  }

}
