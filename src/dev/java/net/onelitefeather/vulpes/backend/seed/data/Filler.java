package net.onelitefeather.vulpes.backend.seed.data;

import net.datafaker.Faker;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Deterministic source of filler values.
 *
 * <p>Both Datafaker and the helper methods draw from one {@link Random} created from a fixed seed,
 * so the same seed always produces the same sequence as long as calls happen in the same order.
 */
public final class Filler {

    private final Random random;
    private final Faker faker;

    public Filler(long seed) {
        this.random = new Random(seed);
        this.faker = new Faker(Locale.ENGLISH, this.random);
    }

    public Faker faker() {
        return faker;
    }

    public <T> T pick(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    /**
     * Returns a value between {@code min} and {@code max}, both inclusive.
     */
    public int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public float between(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public boolean chance(double probability) {
        return random.nextDouble() < probability;
    }
}
