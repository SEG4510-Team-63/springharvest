package dev.springharvest.testing.domains.integration.crud.domains.base.clients;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import dev.springharvest.shared.domains.base.models.dtos.BaseDTO;
import dev.springharvest.testing.domains.integration.crud.domains.base.clients.uri.CrudUriFactory;
import dev.springharvest.testing.domains.integration.shared.domains.base.clients.RestClientImpl;
import io.restassured.response.ValidatableResponse;

class AbstractCrudClientImplTest {

    @Mock
    private RestClientImpl clientHelper;

    @Mock
    private CrudUriFactory uriFactory;

    @Mock
    private ValidatableResponse mockResponse;

    private TestCrudClient testCrudClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testCrudClient = new TestCrudClient(clientHelper, uriFactory);
    }

    @Test
    void testFindAll() {
        when(uriFactory.getFindAllUri(null, null, null)).thenReturn("findAllUri");
        when(clientHelper.getAndThen("findAllUri")).thenReturn(mockResponse);

        ValidatableResponse response = testCrudClient.findAll();

        verify(clientHelper).getAndThen("findAllUri");
        assertEquals(mockResponse, response, "The response should match the mocked response");
    }

    @Test
    void testFindAllAndExtract() {
        when(uriFactory.getFindAllUri(null, null, null)).thenReturn("findAllUri");
        when(clientHelper.getAndThen("findAllUri")).thenReturn(mockResponse);
        when(mockResponse.statusCode(200)).thenReturn(mockResponse);
        when(mockResponse.extract().body().jsonPath().getList("content", TestDTO.class))
            .thenReturn(List.of(new TestDTO(), new TestDTO()));

        List<TestDTO> result = testCrudClient.findAllAndExtract(null, null, null);

        verify(clientHelper).getAndThen("findAllUri");
        assertEquals(2, result.size(), "The result size should match the mocked data");
    }

    @Test
    void testFindById() {
        when(uriFactory.getFindByIdUri()).thenReturn("findByIdUri");
        when(clientHelper.getAndThen("findByIdUri", 1L)).thenReturn(mockResponse);

        ValidatableResponse response = testCrudClient.findById(1L);

        verify(clientHelper).getAndThen("findByIdUri", 1L);
        assertEquals(mockResponse, response, "The response should match the mocked response");
    }

    @Test
    void testCreate() {
        TestDTO dto = new TestDTO();
        when(uriFactory.getPostUri()).thenReturn("postUri");
        when(clientHelper.postAndThen("postUri", dto)).thenReturn(mockResponse);

        ValidatableResponse response = testCrudClient.create(dto);

        verify(clientHelper).postAndThen("postUri", dto);
        assertEquals(mockResponse, response, "The response should match the mocked response");
    }

    @Test
    void testCreateAndExtract() {
        TestDTO dto = new TestDTO();
        when(uriFactory.getPostUri()).thenReturn("postUri");
        when(clientHelper.postAndThen("postUri", dto)).thenReturn(mockResponse);
        when(mockResponse.statusCode(200)).thenReturn(mockResponse);
        when(mockResponse.extract().body().jsonPath().getObject("", TestDTO.class)).thenReturn(dto);

        TestDTO result = testCrudClient.createAndExtract(dto);

        verify(clientHelper).postAndThen("postUri", dto);
        assertEquals(dto, result, "The result should match the input DTO");
    }

    @Test
    void testDeleteByIdAndExtract() {
        when(uriFactory.getDeleteByIdUri()).thenReturn("deleteByIdUri");
        when(clientHelper.deleteAndThen("deleteByIdUri", 1L)).thenReturn(mockResponse);

        testCrudClient.deleteByIdAndExtract(1L);

        verify(clientHelper).deleteAndThen("deleteByIdUri", 1L);
        verify(mockResponse).statusCode(204);
    }

    @Test
    void testCountAndExtract() {
        when(uriFactory.getCountUri()).thenReturn("countUri");
        when(clientHelper.getAndThen("countUri")).thenReturn(mockResponse);
        when(mockResponse.statusCode(200)).thenReturn(mockResponse);
        when(mockResponse.extract().as(Integer.class)).thenReturn(5);

        int count = testCrudClient.countAndExtract();

        verify(clientHelper).getAndThen("countUri");
        assertEquals(5, count, "The count should match the mocked value");
    }

    @Test
    void testUpdateAndExtract() {
        TestDTO dto = new TestDTO();
        when(uriFactory.getPatchUri()).thenReturn("patchUri");
        when(clientHelper.patchAndThen("patchUri", dto, 1L)).thenReturn(mockResponse);
        when(mockResponse.statusCode(200)).thenReturn(mockResponse);
        when(mockResponse.extract().body().jsonPath().getObject("", TestDTO.class)).thenReturn(dto);

        TestDTO result = testCrudClient.updateAndExtract(1L, dto);

        verify(clientHelper).patchAndThen("patchUri", dto, 1L);
        assertEquals(dto, result, "The result should match the input DTO");
    }

   
    static class TestCrudClient extends AbstractCrudClientImpl<TestDTO, Long> {
        public TestCrudClient(RestClientImpl clientHelper, CrudUriFactory uriFactory) {
            super(clientHelper, uriFactory, TestDTO.class);
        }
    }

    
    static class TestDTO extends BaseDTO<Long> {
        private Long id;

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }
    }
}
