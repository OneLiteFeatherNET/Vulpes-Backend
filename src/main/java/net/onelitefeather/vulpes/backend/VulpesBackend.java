package net.onelitefeather.vulpes.backend;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "Vulpes Backend",
                version = "1.0",
                description = """
                        Vulpes Backend API handles the custom attributes, fonts, items and notifications API for Dungeon Project.
                        It provides endpoints to manage custom attributes, fonts, items, and notifications.
                        The API is designed to be used by the Vulpes Generator to generate dependencies for Minestom Server"""
        ),
        tags = {
                @Tag(name = "Attribute", description = "Custom Minecraft Attribute API"),
                @Tag(name = "Font", description = "Custom Minecraft Font API"),
                @Tag(name = "Item", description = "Custom Minecraft Item API"),
                @Tag(name = "Notification", description = "Custom Minecraft Notification API"),
        },
        // Applied to every operation. The exceptions -- health, metrics and the Swagger paths --
        // are not documented operations, so nothing is misdescribed by declaring it globally.
        security = @SecurityRequirement(name = VulpesBackend.BEARER_SCHEME)
)
@SecurityScheme(
        name = VulpesBackend.BEARER_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = """
                A bearer token from the deployment's OpenID Connect provider. The backend validates \
                tokens; it never issues them. Clients obtain one themselves and send it unchanged."""
)
public class VulpesBackend {

    /**
     * The name the generated description gives the bearer scheme, and the handle generated clients
     * use to supply a token.
     */
    public static final String BEARER_SCHEME = "bearerAuth";

    public static void main(String[] args) {
        Micronaut.run(VulpesBackend.class, args);
    }
}
