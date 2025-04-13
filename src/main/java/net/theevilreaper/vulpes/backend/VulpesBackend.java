package net.theevilreaper.vulpes.backend;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Vulpes Backend",
                version = "1.0",
                description = "Vulpes Backend API"
        )
)
public class VulpesBackend {

    public static void main(String[] args) {
        Micronaut.run(VulpesBackend.class, args);
    }
}
