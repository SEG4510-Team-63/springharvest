package dev.springharvest.testing.domains.integration.crud.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.springharvest.shared.domains.base.models.dtos.BaseDTO;
import dev.springharvest.testing.domains.integration.crud.domains.base.clients.AbstractCrudClientImpl;
import dev.springharvest.testing.domains.integration.crud.domains.base.clients.ICrudClient;
import dev.springharvest.testing.domains.integration.shared.domains.base.factories.IPKModelFactory;
import dev.springharvest.testing.domains.integration.shared.tests.AbstractBaseIT;
import io.restassured.response.ValidatableResponse;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public abstract class AbstractCrudIT<D extends BaseDTO<K>, K extends Serializable>
    extends AbstractBaseIT
    implements ICrudIT<D, K> {

  protected ICrudClient<D, K> client;

  protected IPKModelFactory<D, K> modelFactory;


  protected AbstractCrudIT(AbstractCrudClientImpl<D, K> client, IPKModelFactory<D, K> modelFactory) {
    this.client = client;
    this.modelFactory = modelFactory;
  }

  @Nested
  class ConfigTest {

    @Test
    void contextLoads() {
      assertTrue(true);
    }

  }

  @Nested
  class CrudPaths {

    @Nested
    class PostPaths {

      @Test
      void canPostOne() {

        int expectedResponseCode = 500;
        ValidatableResponse response = client.create(modelFactory.buildInvalidDto());
        response.statusCode(expectedResponseCode);

        D toCreate = modelFactory.buildValidDto();
        D created = client.createAndExtract(toCreate);

        toCreate.setId(created.getId());
        SoftAssertions softly = new SoftAssertions();
        modelFactory.softlyAssert(softly, toCreate, created);
        softly.assertAll();
      }

      /**
       * Tests if a list of entities can be created by the API.
       */
      @Test
      void canPostMany() {
        D toCreate = modelFactory.buildValidDto();
        List<D> allCreated = client.createAllAndExtract(List.of(toCreate));

        D lastCreated = allCreated.get(allCreated.size() - 1);
        K id = lastCreated.getId();
        toCreate.setId(id);

        SoftAssertions softly = new SoftAssertions();
        modelFactory.softlyAssert(softly, toCreate, lastCreated);
        softly.assertAll();

      }

    }

    @Nested
    class GetPaths {

      private static Stream<Arguments> findAllArgumentsProvider() {
        List<Boolean> provideDefaultEmptyArguments = List.of(Boolean.FALSE, Boolean.TRUE);
        List<Integer> pageSizeProvider = List.of(0, 1, 10, 100, Integer.MIN_VALUE, Integer.MAX_VALUE);
        List<Integer> pageNumberProvider = List.of(0, 1, 2, Integer.MIN_VALUE, Integer.MAX_VALUE);
        List<String> sortsProvider = List.of("id-asc", "id-desc", "id-asc,id-desc", ",", "invalidField-asc", "id-invalidDirection", "id",
                                             RandomStringUtils.randomNumeric(5));

        return provideDefaultEmptyArguments.stream()
            .flatMap(isDefaultEmptyArgument -> pageSizeProvider.stream()
                .flatMap(pageSize -> pageNumberProvider.stream()
                    .flatMap(pageNumber -> sortsProvider.stream()
                        .map(sorts -> Arguments.of(isDefaultEmptyArgument, pageSize, pageNumber, sorts)))));
      }

      @Test
      void canFindById() {
        List<D> dtos = client.findAllAndExtract();
        Assertions.assertFalse(dtos.isEmpty());
        D firstDto = dtos.get(0);
        D dto = client.findByIdAndExtract(firstDto.getId());
        assertNotNull(dto);
      }

      @ParameterizedTest
      @MethodSource("findAllArgumentsProvider")
      void canFindAll(Boolean isDefaultEmptyArguments, Integer pageSize, Integer pageNumber, String sorts) {
        client.deleteAllByIds(client.findAllAndExtract().stream().map(BaseDTO::getId).toList());
        List<D> createdDtos = client.createAllAndExtract(modelFactory.buildValidDto(2));
        List<D> dtos = isDefaultEmptyArguments ? client.findAllAndExtract() : client.findAllAndExtract(pageSize, pageNumber, sorts);
        Assertions.assertEquals(dtos.size(), createdDtos.size());
      }
    }

    @Nested
    class PatchPaths {

      @Test
      void canUpdateOne() {
        List<D> dtos = client.findAllAndExtract();
        Assertions.assertFalse(dtos.isEmpty());
        D firstDto = dtos.get(0);
        K id = firstDto.getId();
        firstDto = modelFactory.buildValidUpdatedDto(id);
        D updated = client.updateAndExtract(id, firstDto);
        D retrieved = client.findByIdAndExtract(id);
        SoftAssertions softly = new SoftAssertions();
        modelFactory.softlyAssert(softly, updated, retrieved);
        softly.assertAll();

      }

      @Test
      void canUpdateMany() {
        List<D> dtos = client.findAllAndExtract();
        Assertions.assertFalse(dtos.isEmpty());
        D firstDto = dtos.get(0);
        List<D> toUpdate = List.of(modelFactory.buildValidUpdatedDto(firstDto));
        List<D> updated = client.updateAllAndExtract(toUpdate);
        SoftAssertions softly = new SoftAssertions();
        modelFactory.softlyAssert(softly, toUpdate, updated);
        softly.assertAll();
      }

    }

    @Nested
    class DeletePaths {

      @Test
      void canDeleteOneAndCanExistsById() {
        D created = client.createAndExtract(modelFactory.buildValidDto());
        client.deleteById(created.getId()).statusCode(204);
      }

      @Test
      void canDeleteAllByIds() {
        D created = client.createAndExtract(modelFactory.buildValidDto());
        client.deleteAllByIds(List.of(created.getId())).statusCode(204);
      }

    }

  }

}