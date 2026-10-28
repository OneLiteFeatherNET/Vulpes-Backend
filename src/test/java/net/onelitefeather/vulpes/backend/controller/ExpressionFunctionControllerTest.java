package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.controller.expression.ExpressionFunctionController;
import net.onelitefeather.vulpes.backend.controller.expression.ProjectExpressionCatalogController;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionCatalogDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionEvaluateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionInterfaceDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionKind;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionFunctionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidateDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionValidationResultDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionEntity;
import net.onelitefeather.vulpes.backend.entity.expression.ExpressionFunctionEntity;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ExpressionFunctionService;
import net.onelitefeather.vulpes.backend.service.ExpressionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ExpressionFunctionController (implementation tests)")
class ExpressionFunctionControllerTest {

    private static class StubExpressionFunctionService implements ExpressionFunctionService {
        ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO response;
        Optional<ExpressionFunctionEntity> findByIdResponse = Optional.empty();
        Page<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> modelDtoPage;
        List<ExpressionFunctionDTO> definitions = List.of();
        ExpressionEvaluationResultDTO evaluationResponse;
        ExpressionFunctionInterfaceDTO interfaceResponse;

        @Override
        public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO create(ExpressionFunctionDTO dto) {
            return response;
        }

        @Override
        public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO update(ExpressionFunctionDTO dto) {
            return response;
        }

        @Override
        public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> getAll(Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<ExpressionFunctionEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO create(UUID projectId, ExpressionFunctionDTO dto) {
            return response;
        }

        @Override
        public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO update(UUID projectId, ExpressionFunctionDTO dto) {
            return response;
        }

        @Override
        public ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> getAll(UUID projectId, Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<ExpressionFunctionEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }

        @Override
        public List<ExpressionFunctionDTO> definitions(UUID projectId) {
            return definitions;
        }

        @Override
        public ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionFunctionEvaluateDTO request) {
            return evaluationResponse;
        }

        @Override
        public ExpressionFunctionInterfaceDTO interfaceSource(UUID projectId) {
            return interfaceResponse;
        }
    }

    private static class StubExpressionService implements ExpressionService {
        ExpressionCatalogDTO catalogResponse;

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO create(ExpressionDTO dto) {
            return null;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO update(ExpressionDTO dto) {
            return null;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO delete(UUID id) {
            return null;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<ExpressionResponseDTO.ExpressionModelDTO> getAll(Pageable pageable) {
            return null;
        }

        @Override
        public Optional<ExpressionEntity> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO create(UUID projectId, ExpressionDTO dto) {
            return null;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO update(UUID projectId, ExpressionDTO dto) {
            return null;
        }

        @Override
        public ExpressionResponseDTO.ExpressionModelDTO delete(UUID projectId, UUID id) {
            return null;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<ExpressionResponseDTO.ExpressionModelDTO> getAll(UUID projectId, Pageable pageable) {
            return null;
        }

        @Override
        public Optional<ExpressionEntity> findById(UUID projectId, UUID id) {
            return Optional.empty();
        }

        @Override
        public ExpressionEvaluationResultDTO evaluate(UUID projectId, ExpressionEvaluateDTO request) {
            return null;
        }

        @Override
        public ExpressionValidationResultDTO validate(UUID projectId, ExpressionValidateDTO request) {
            return null;
        }

        @Override
        public ExpressionCatalogDTO catalog() {
            return null;
        }

        @Override
        public ExpressionCatalogDTO catalog(UUID projectId) {
            return catalogResponse;
        }
    }

    private static ProjectEntity sampleProject(UUID id) {
        return new ProjectEntity(id, "Test Project", "test-project", null, null, null, false);
    }

    private static ExpressionFunctionDTO sampleDTO(UUID id) {
        return new ExpressionFunctionDTO(
                id,
                "Damage bonus",
                "damage_bonus",
                ExpressionFunctionKind.INTERFACE,
                ExpressionVariableType.NUMBER,
                List.of(new ExpressionVariableDTO("attack", ExpressionVariableType.NUMBER, "3")),
                null,
                null
        );
    }

    @Test
    void testAdd_returnsServiceFunction() {
        StubExpressionFunctionService stub = new StubExpressionFunctionService();
        UUID projectId = UUID.randomUUID();
        ExpressionFunctionDTO dto = sampleDTO(null);
        ExpressionFunctionEntity entity = dto.toEntity(sampleProject(projectId));
        stub.response = ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.createDTO(entity);

        HttpResponse<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> resp =
                new ExpressionFunctionController(stub).add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode(), "add must answer 200");
        assertEquals("damage_bonus", resp.body().name(), "add must return the service's function");
    }

    @Test
    void testGetById_notFound_throwsNotFound() {
        StubExpressionFunctionService stub = new StubExpressionFunctionService();

        ApiException exception = assertThrows(ApiException.class,
                () -> new ExpressionFunctionController(stub).getById(UUID.randomUUID(), UUID.randomUUID()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code(), "an unknown function must answer RESOURCE_NOT_FOUND");
    }

    @Test
    void testRemove_returnsServiceFunction() {
        StubExpressionFunctionService stub = new StubExpressionFunctionService();
        UUID projectId = UUID.randomUUID();
        stub.response = ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO.createDTO(
                sampleDTO(null).toEntity(sampleProject(projectId)));

        HttpResponse<ExpressionFunctionResponseDTO.ExpressionFunctionModelDTO> resp =
                new ExpressionFunctionController(stub).remove(projectId, UUID.randomUUID());

        assertEquals(200, resp.getStatus().getCode(), "remove must answer 200 with the removed function");
        assertEquals("damage_bonus", resp.body().name(), "remove must return the removed function");
    }

    @Test
    void testEvaluate_returnsServiceResult() {
        StubExpressionFunctionService stub = new StubExpressionFunctionService();
        stub.evaluationResponse = new ExpressionEvaluationResultDTO("30", "NUMBER");

        HttpResponse<ExpressionEvaluationResultDTO> resp = new ExpressionFunctionController(stub)
                .evaluate(UUID.randomUUID(), new ExpressionFunctionEvaluateDTO(sampleDTO(null), Map.of("attack", "3")));

        assertEquals(200, resp.getStatus().getCode(), "evaluate must answer 200");
        assertEquals("30", resp.body().value(), "evaluate must return the service's result");
    }

    @Test
    void testGeneratedInterface_returnsServiceSource() {
        StubExpressionFunctionService stub = new StubExpressionFunctionService();
        stub.interfaceResponse = new ExpressionFunctionInterfaceDTO("java", "ProjectExpressionFunctions", "public interface X {}");

        HttpResponse<ExpressionFunctionInterfaceDTO> resp = new ExpressionFunctionController(stub)
                .generatedInterface(UUID.randomUUID());

        assertEquals(200, resp.getStatus().getCode(), "the interface must answer 200");
        assertEquals("java", resp.body().language(), "the interface must return the service's source");
    }

    @Test
    void testProjectCatalog_returnsServiceCatalog() {
        StubExpressionService stub = new StubExpressionService();
        stub.catalogResponse = new ExpressionCatalogDTO("3.8.0", List.of(), List.of(), List.of());

        HttpResponse<ExpressionCatalogDTO> resp = new ProjectExpressionCatalogController(stub)
                .catalog(UUID.randomUUID());

        assertEquals(200, resp.getStatus().getCode(), "the project catalog must answer 200");
        assertEquals("3.8.0", resp.body().evalexVersion(), "the project catalog must return the service's catalog");
    }
}
