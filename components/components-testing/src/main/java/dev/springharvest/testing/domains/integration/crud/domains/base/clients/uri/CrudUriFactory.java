package dev.springharvest.testing.domains.integration.crud.domains.base.clients.uri;


import dev.springharvest.crud.domains.base.rest.constants.CrudControllerUri;
import dev.springharvest.testing.domains.integration.shared.domains.base.clients.uri.UriUtils;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


@Getter
public class CrudUriFactory implements ICrudUriFactory {

  private final String domainContext;

  public CrudUriFactory(String domainContext) {
    this.domainContext = domainContext;
  }

  @Override
  public String getExistsByIdUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.EXISTS_BY_ID);
  }

  @Override
  public String getFindByIdUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.FIND_BY_ID);
  }

  @Override
  public String getFindAllUri(@Nullable Integer size, @Nullable Integer page, @Nullable String sorts) {
    StringBuilder params = new StringBuilder();
    if (size != null && page != null && StringUtils.isNotBlank(sorts)) {
      params.append("?").append(size);
    }
    if (size != null) {
      params.append("size=").append(size);
    }
    if (page != null) {
      params.append("&page=").append(page);
    }
    if (StringUtils.isNotBlank(sorts)) {
      params.append("&sorts=").append(sorts);
    }
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.FIND_ALL + params);
  }

  @Override
  public String getPostUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.CREATE);
  }

  @Override
  public String getPostAllUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.CREATE_ALL);
  }

  @Override
  public String getPatchUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.UPDATE_BY_ID);
  }

  @Override
  public String getPatchAllUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.UPDATE_ALL);
  }

  @Override
  public String getDeleteByIdUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.DELETE_BY_ID);
  }

  @Override
  public String getDeleteAllByIdsUri() {
    return UriUtils.buildUri(getDomainContext(), CrudControllerUri.DELETE_ALL);
  }


}