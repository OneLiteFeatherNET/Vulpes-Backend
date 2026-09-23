package net.onelitefeather.vulpes.backend.seed.fixture;

import net.onelitefeather.vulpes.backend.seed.SeedWriter;
import net.onelitefeather.vulpes.backend.seed.data.Filler;

/**
 * One seeded project with all of its content.
 */
public interface ProjectFixture {

    void seed(SeedWriter writer, Filler filler);
}
