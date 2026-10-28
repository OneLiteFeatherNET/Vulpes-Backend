package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.controller.expression.ExpressionCatalogController;
import net.onelitefeather.vulpes.backend.controller.expression.ExpressionController;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionEntity;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ExpressionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ExpressionController (implementation tests)")
class ExpressionControllerTest {

    private static final Faker FAKER = new Faker();

    private static class StubExpressionService implements ExpressionService {
        ExpressionResponseDTO.ExpressionModelDTO response;
        Optional<ExpressionEntity> findByIdResponse;
        Page<ExpressionResponseDTO.ExpressionModelDTO> modelDtoPage;
        ExpressionEvaluationResultDTO evaluationResponse;
        ExpressionValidationResultDTO validationResponse;
        ExpressionCatalogDTO catalogResponse;

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO create(ExpressionDTO dto) {
            return response;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO update(ExpressionDTO dto) {
            return response;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<ExpressionResponseDTO.ExpressionModelDTO> getAll(Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<ExpressionEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO create(UUID projectId, ExpressionDTO dto) {
            return response;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO update(UUID projectId, ExpressionDTO dto) {
            return response;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<ExpressionResponseDTO.ExpressionModelDTO> getAll(UUID projectId, Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<ExpressionEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }

        @Override
        public ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionEvaluateDTO request) {
            return evaluationResponse;
        }

        @Override
        public ExpressionValidationResultDTO validate(UUID projectId, ExpressionValidateDTO request) {
            return validationResponse;
        }

        @Override
        public ExpressionCatalogDTO catalog() {
            return catalogResponse;
        }

        @Override
        public ExpressionCatalogDTO catalog(UUID projectId) {
            return catalogResponse;
        }
    }

    private static ProjectEntity sampleProject(UUID id) {
        return new ProjectEntity(id, "Test Project", "test-project", null, null, null, false);
    }

    private static ExpressionDTO sampleDTO(UUID id) {
        return new ExpressionDTO(
                id,
                FAKER.rockBand().name(),
                "a * 2 + b",
                null,
                List.of(new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, "3")),
                null,
                null,
                null
        );
    }

    @Test
    void testAdd_returnsOk() {
        StubExpressionService stub = new StubExpressionService();
        UUID projectId = UUID.randomUUID();
        ExpressionDTO dto = sampleDTO(UUID.randomUUID());
        stub.response = ExpressionResponseDTO.ExpressionModelDTO.createDTO(dto.toEntity(sampleProject(projectId)));

        HttpResponse<ExpressionResponseDTO.ExpressionModelDTO> resp = new ExpressionController(stub).add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode(), "add must answer 200");
        assertEquals(stub.response.id(), resp.body().id(), "add must return the service's expression");
    }

    @Test
    void testGetById_found_returnsOk() {
        StubExpressionService stub = new StubExpressionService();
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        stub.findByIdResponse = Optional.of(sampleDTO(id).toEntity(sampleProject(projectId)));

        HttpResponse<ExpressionResponseDTO.ExpressionModelDTO> resp = new ExpressionController(stub).getById(projectId, id);

        assertEquals(200, resp.getStatus().getCode(), "getById must answer 200 for an owned expression");
        assertEquals(id, resp.body().id(), "getById must return the requested expression");
    }

    @Test
    void testGetById_notFound_raisesNotFound() {
        StubExpressionService stub = new StubExpressionService();
        stub.findByIdResponse = Optional.empty();
        ExpressionController controller = new ExpressionController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code(), "an unknown expression must answer RESOURCE_NOT_FOUND");
        assertEquals("Expression not found.", exception.detail(), "the detail must name the expression");
    }

    @Test
    void testRemove_notFound_propagates() {
        StubExpressionService stub = new StubExpressionService() {
            @Override
            public ExpressionResponseDTO.ExpressionModelDTO delete(UUID projectId, UUID id) {
                throw ApiException.notFound("Expression");
            }
        };
        ExpressionController controller = new ExpressionController(stub);

        ApiException exception = assertThrows(ApiException.class,
                () -> controller.remove(UUID.randomUUID(), UUID.randomUUID()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code(), "a missing expression must reach the exception handler");
    }

    @Test
    void testGetAll_returnsPageOk() {
        StubExpressionService stub = new StubExpressionService();
        UUID projectId = UUID.randomUUID();
        ExpressionResponseDTO.ExpressionModelDTO model = ExpressionResponseDTO.ExpressionModelDTO.createDTO(
                sampleDTO(UUID.randomUUID()).toEntity(sampleProject(projectId)));
        stub.modelDtoPage = Page.of(List.of(model), Pageable.from(0, 10), 1L);

        HttpResponse<Page<ExpressionResponseDTO.ExpressionModelDTO>> resp =
                new ExpressionController(stub).getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode(), "getAll must answer 200");
        assertEquals(1, resp.body().getTotalSize(), "getAll must report the total size of the page");
        assertEquals(model.id(), resp.body().getContent().getFirst().id(), "getAll must return the page content");
    }

    @Test
    void testEvaluate_returnsServiceResult() {
        StubExpressionService stub = new StubExpressionService();
        stub.evaluationResponse = new ExpressionEvaluationResultDTO("7", "NUMBER");

        HttpResponse<ExpressionEvaluationResultDTO> resp = new ExpressionController(stub)
                .evaluate(UUID.randomUUID(), new ExpressionEvaluateDTO("a * 2 + b", null, null));

        assertEquals(200, resp.getStatus().getCode(), "evaluate must answer 200");
        assertEquals("7", resp.body().value(), "evaluate must return the value the service computed");
        assertEquals("NUMBER", resp.body().type(), "evaluate must return the type the service computed");
    }

    @Test
    void testValidate_returnsServiceResult() {
        StubExpressionService stub = new StubExpressionService();
        stub.validationResponse = new ExpressionValidationResultDTO(
                false,
                List.of(new ExpressionValidationResultDTO.ErrorDTO("Unexpected token", 4, 5)),
                List.of(),
                List.of(),
                List.of());

        HttpResponse<ExpressionValidationResultDTO> resp = new ExpressionController(stub)
                .validate(UUID.randomUUID(), new ExpressionValidateDTO("a + * b", List.of("a", "b")));

        assertEquals(200, resp.getStatus().getCode(), "validate must answer 200 even when the expression does not parse");
        assertFalse(resp.body().valid(), "validate must return the service's verdict");
        assertEquals(4, resp.body().errors().getFirst().start(), "validate must return the service's error position");
    }

    @Test
    void testCatalog_returnsServiceCatalog() {
        StubExpressionService stub = new StubExpressionService();
        stub.catalogResponse = new ExpressionCatalogDTO("3.8.0", List.of(), List.of(), List.of());

        HttpResponse<ExpressionCatalogDTO> resp = new ExpressionCatalogController(stub).catalog();

        assertEquals(200, resp.getStatus().getCode(), "catalog must answer 200");
        assertEquals("3.8.0", resp.body().evalexVersion(), "catalog must return the service's catalog");
    }
}
