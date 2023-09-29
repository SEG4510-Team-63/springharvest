package dev.springharvest.library.domains.authors.mappers.search;

import dev.springharvest.library.beans.GlobalClazzResolver;
import dev.springharvest.library.domains.authors.models.entities.AuthorEntityMetadata;
import dev.springharvest.library.domains.authors.models.queries.AuthorFilterBO;
import dev.springharvest.library.domains.authors.models.queries.AuthorFilterDTO;
import dev.springharvest.library.domains.authors.models.queries.AuthorFilterRequestBO;
import dev.springharvest.library.domains.authors.models.queries.AuthorFilterRequestDTO;
import dev.springharvest.search.mappers.queries.ISearchMapper;
import java.util.Set;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public abstract class AuthorSearchMapper
    implements ISearchMapper<AuthorFilterRequestDTO, AuthorFilterRequestBO, AuthorFilterDTO, AuthorFilterBO> {

  @Autowired
  private GlobalClazzResolver globalClazzResolver;

  @Autowired
  private AuthorEntityMetadata entityMetadata;


  @Override
  public Class<?> getClazz(String path) {
    return globalClazzResolver.getClazz(path);
  }

  @Override
  public Set<String> getRoots() {
    return entityMetadata.getRootPaths();
  }

}