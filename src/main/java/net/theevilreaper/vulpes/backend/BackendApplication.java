package net.theevilreaper.vulpes.backend;

import io.micronaut.context.annotation.Import;
import io.micronaut.runtime.Micronaut;

@Import(
        packages = "net.theevilreaper.vulpes.*",
        annotated = "*"
)
public class BackendApplication {

    public static void main(String[] args) {
        Micronaut.run(BackendApplication.class, args);
    }
}
