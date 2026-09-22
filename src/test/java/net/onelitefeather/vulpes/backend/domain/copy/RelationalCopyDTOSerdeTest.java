package net.onelitefeather.vulpes.backend.domain.copy;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves that {@link RelationalCopyDTO}, a generic {@code @Serdeable} record, round-trips
 * correctly through Micronaut's serde machinery when parameterized with {@link ItemRelation}.
 * The project has no prior generic {@code @Serdeable} type, so this is verified rather than
 * assumed.
 *
 * <p>Runs against a context with the datasource disabled rather than {@code @MicronautTest}:
 * this is a pure serde concern that never touches the database, and every {@code @MicronautTest}
 * in this project eagerly boots Hibernate's {@code SessionFactory}, which requires Testcontainers
 * and Docker registry access it doesn't need here.
 */
@DisplayName("Serde round-trip for the generic RelationalCopyDTO<ItemRelation>")
class RelationalCopyDTOSerdeTest {

    @Test
    @DisplayName("deserializes a JSON body into RelationalCopyDTO<ItemRelation> with relations intact")
    void deserializesGenericRelations() throws IOException {
        try (ApplicationContext context = ApplicationContext.run(Map.of("datasources.default.enabled", false))) {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

            UUID targetProjectId = UUID.randomUUID();
            String json = """
                    {"targetProjectId":"%s","targetKey":"new-key","targetName":"New Name","relations":["LORE","FLAGS"]}
                    """.formatted(targetProjectId);

            RelationalCopyDTO<ItemRelation> dto = objectMapper.readValue(
                    json,
                    Argument.of(RelationalCopyDTO.class, ItemRelation.class)
            );

            assertEquals(targetProjectId, dto.targetProjectId());
            assertEquals("new-key", dto.targetKey());
            assertEquals("New Name", dto.targetName());
            assertEquals(Set.of(ItemRelation.LORE, ItemRelation.FLAGS), dto.relations());
        }
    }
}
