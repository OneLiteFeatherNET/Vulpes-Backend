package net.onelitefeather.vulpes.backend;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
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
        }
)
public class VulpesBackend {

    public static void main(String[] args) {
        Micronaut.run(VulpesBackend.class, args);
    }
}
