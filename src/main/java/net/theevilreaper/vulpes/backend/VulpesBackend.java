package net.theevilreaper.vulpes.backend;

import io.micronaut.context.annotation.Import;
import io.micronaut.runtime.Micronaut;

@Import(
        packages = "net.theevilreaper.vulpes.*",
        annotated = "*"
)
public class VulpesBackend {

    public static void main(String[] args) {
        Micronaut.run(VulpesBackend.class, args);
    }
}
