package net.onelitefeather.vulpes.backend.seed.fixture;

import net.onelitefeather.vulpes.backend.seed.SeedWriter;
import net.onelitefeather.vulpes.backend.seed.data.Filler;

/**
 * A project without any entities, for the empty states of all list screens.
 */
public final class EmptyProjectFixture implements ProjectFixture {

    public static final String PROJECT_KEY = "empty_project";

    @Override
    public void seed(SeedWriter w, Filler filler) {
        w.project(PROJECT_KEY, "Empty Project", "Intentionally left empty.", false, null, null);
    }
}
