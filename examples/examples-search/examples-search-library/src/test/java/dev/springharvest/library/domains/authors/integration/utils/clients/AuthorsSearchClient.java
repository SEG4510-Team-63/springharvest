package dev.springharvest.library.domains.authors.integration.utils.clients;

import dev.springharvest.library.domains.authors.models.queries.AuthorFilterRequestDTO;
import dev.springharvest.shared.domains.authors.constants.AuthorConstants;
import dev.springharvest.shared.domains.authors.models.dtos.AuthorDTO;
import dev.springharvest.shared.domains.authors.models.entities.AuthorEntity;
import dev.springharvest.testing.integration.search.clients.AbstractSearchClientImpl;
import dev.springharvest.testing.integration.shared.clients.RestClientImpl;
import dev.springharvest.testing.integration.shared.uri.UriFactory;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorsSearchClient extends AbstractSearchClientImpl<AuthorDTO, AuthorEntity, UUID, AuthorFilterRequestDTO> {

  @Autowired
  protected AuthorsSearchClient(RestClientImpl restClient) {
    super(restClient, new UriFactory(AuthorConstants.Controller.DOMAIN_CONTEXT), AuthorDTO.class);
  }

}
