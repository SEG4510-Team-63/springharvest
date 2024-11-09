package dev.springharvest.library.domains.pet.graphql;

import dev.springharvest.crud.domains.base.graphql.AbstractGraphQLCrudController;
import dev.springharvest.library.domains.pet.models.dtos.PetDTO;
import dev.springharvest.library.domains.pet.models.entities.PetEntity;
import dev.springharvest.library.domains.pet.service.PetQueryCrudService;
import dev.springharvest.shared.constants.DataPaging;
import dev.springharvest.shared.constants.PageData;
import dev.springharvest.shared.domains.base.mappers.IBaseModelMapper;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class PetGraphQLController extends AbstractGraphQLCrudController<PetDTO, PetEntity, UUID> {

    @Autowired
    public  PetGraphQLController(IBaseModelMapper<PetDTO, PetEntity, UUID> modelMapper, PetQueryCrudService baseService){
        super(modelMapper, baseService, PetEntity.class);
    }

    @QueryMapping
    public PageData<PetDTO> searchPets(@Argument Map<String, Object> filter, @Argument Map<String, Object> clause, @Argument DataPaging paging, DataFetchingEnvironment environment) {
        return search(filter, clause, paging, environment);
    }

    @QueryMapping
    public long countPets(@Argument Map<String, Object> filter, @Argument Map<String, Object> clause, @Argument List<String> fields) {
        return count(filter, clause, fields);
    }
}
