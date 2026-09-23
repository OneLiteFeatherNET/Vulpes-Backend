package net.onelitefeather.vulpes.backend.seed.fixture;

import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.seed.SeedWriter;
import net.onelitefeather.vulpes.backend.seed.data.Filler;
import net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog;

import java.util.List;
import java.util.Locale;

import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.CLOCK_END;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.ENCHANTMENTS;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.FRAME_TYPES;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.INFINIBURN_END;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.ITEM_FLAGS;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.ITEM_GROUPS;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.MATERIALS;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.SOUND_EVENTS;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.SOUND_SOURCE_TYPES;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.TIMELINES;

/**
 * "Edge Cases": data that is awkward on purpose. Named records cover empty child collections,
 * boundary values, long and formatted texts, enum coverage and long ordered lists; bulk records
 * push every list past one API page.
 */
public final class EdgeCaseFixture implements ProjectFixture {

    public static final String PROJECT_KEY = "edge_cases";

    /**
     * Records per top-level entity type; above the default API page size of 100.
     */
    public static final int BULK_SIZE = 120;

    /**
     * Column length of every string column in the schema.
     */
    private static final int MAX_TEXT = 255;

    private static final String UNICODE = "Ünïcödé ⚔ 日本語 Ελληνικά 🗡️";

    @Override
    public void seed(SeedWriter w, Filler f) {
        ProjectEntity p = w.project(PROJECT_KEY, "Edge Cases",
                longText("Boundary values, empty collections and bulk data. "), false,
                "https://example.net/" + "very-long-path/".repeat(10), "https://docs.example.net/edge-cases");

        seedItems(w, f, p);
        seedFonts(w, f, p);
        seedSounds(w, f, p);
        seedDimensions(w, f, p);
        seedAttributes(w, f, p);
        seedNotifications(w, f, p);
    }

    private static void seedItems(SeedWriter w, Filler f, ProjectEntity p) {
        w.item(p, "no_children")
                .uiName("No Children").displayName("Plain Stone").comment("No lore, enchantments or flags")
                .save();
        w.item(p, "long_texts")
                .uiName(longText("Long UI name ")).displayName(longText("§c§lLong §r§edisplay §kname "))
                .material("minecraft:enchanted_golden_apple").group(longText("group-"))
                .comment(longText("A comment that fills the whole column. "))
                .lore(longText("§7A lore line that fills the whole column. "))
                .save();
        w.item(p, "unicode")
                .uiName(UNICODE).displayName("§d" + UNICODE).material("minecraft:amethyst_shard")
                .comment("Emoji, CJK, Greek and umlauts: " + UNICODE)
                .lore("§a" + UNICODE, "§4§l§nFormatted §r§o§mcodes §k#####")
                .save();
        w.item(p, "many_lore_lines")
                .uiName("Many Lore Lines").displayName("§fChronicle").material("minecraft:writable_book")
                .group("quest").lore(numbered("§7Chapter %02d: ", 30, f))
                .save();
        w.item(p, "all_flags")
                .uiName("All Flags").displayName("§8Hidden Tooltip").material("minecraft:diamond_boots")
                .flags(ITEM_FLAGS.toArray(String[]::new))
                .save();
        var allEnchantments = w.item(p, "all_enchantments")
                .uiName("All Enchantments").displayName("§5Everything").material("minecraft:book");
        ENCHANTMENTS.forEach(e -> allEnchantments.enchant(e, e.maxLevel()));
        allEnchantments.save();
        w.item(p, "unsafe_enchantments")
                .uiName("Unsafe Enchantments").displayName("§4Overpowered").material("minecraft:netherite_sword")
                .group("weapon")
                .enchant("minecraft:sharpness", 10, true)
                .enchant("minecraft:knockback", 255, true)
                .enchant("minecraft:efficiency", Short.MAX_VALUE, true)
                .enchant("minecraft:fire_aspect", 1, true)
                .save();
        w.item(p, "number_bounds_min")
                .uiName("Number Bounds (min)").displayName("Minimal").amount(1).customModelData(0)
                .enchant("minecraft:unbreaking", 1)
                .save();
        w.item(p, "number_bounds_max")
                .uiName("Number Bounds (max)").displayName("Maximal").amount(99).customModelData(Integer.MAX_VALUE)
                .save();

        for (int i = 1; i <= BULK_SIZE; i++) {
            var item = w.item(p, bulkKey("item", i))
                    .uiName(f.faker().ancient().hero() + "'s " + f.faker().lorem().word())
                    .displayName("§" + f.pick(COLOR_CODES) + f.faker().ancient().god() + "'s Relic")
                    .material(f.pick(MATERIALS)).group(f.pick(ITEM_GROUPS))
                    .customModelData(f.between(0, 5000)).amount(f.between(1, 64))
                    .comment(f.chance(0.5) ? f.faker().lorem().sentence() : null);
            int enchantments = f.between(0, 2);
            for (int e = 0; e < enchantments; e++) {
                MinecraftCatalog.Enchantment enchantment = f.pick(ENCHANTMENTS);
                item.enchant(enchantment, f.between(1, enchantment.maxLevel()));
            }
            int lore = f.between(0, 3);
            for (int l = 0; l < lore; l++) {
                item.lore("§7" + f.faker().lorem().sentence(6));
            }
            if (f.chance(0.3)) {
                item.flags(f.pick(ITEM_FLAGS));
            }
            item.save();
        }
    }

    private static void seedFonts(SeedWriter w, Filler f, ProjectEntity p) {
        w.font(p, "no_chars").uiName("No Chars").texture("edge:font/empty.png").save();
        w.font(p, "many_chars")
                .uiName("Many Chars").texture("edge:font/grid.png").metrics(0, 0)
                .comment("40 rows for reordering, ascent and height 0")
                .chars(privateUseRows(40, 16, 0xE400))
                .save();
        w.font(p, "long_line")
                .uiName(longText("Long line font ")).texture(longText("edge:font/long_"))
                .metrics(Integer.MAX_VALUE, Integer.MAX_VALUE).comment(longText("Every text column is full. "))
                .chars(privateUseRows(1, MAX_TEXT, 0xE800))
                .save();

        for (int i = 1; i <= BULK_SIZE; i++) {
            w.font(p, bulkKey("font", i))
                    .uiName(f.faker().ancient().titan() + " Glyphs")
                    .texture("edge:font/bulk_" + i + ".png").metrics(f.between(6, 12), f.between(8, 16))
                    .chars(privateUseRows(f.between(1, 3), 8, 0xEA00 + i * 24))
                    .save();
        }
    }

    private static void seedSounds(SeedWriter w, Filler f, ProjectEntity p) {
        w.sound(p, "no_sources").uiName("No Sources").keyName("edge:silence").save();
        w.sound(p, "source_bounds")
                .uiName("Source Bounds").keyName("edge:bounds").subTitle(longText("Subtitle "))
                .source("edge:bounds/min", 0.0f, 0.0f, 1, false, 1, false, "file")
                .source("edge:bounds/max", 1.0f, 2.0f, Integer.MAX_VALUE, true, Integer.MAX_VALUE, true, "file")
                .source("minecraft:ui.button.click", 0.5f, 1.0f, 1, false, 16, false, "event")
                .save();
        var many = w.sound(p, "many_sources").uiName("Many Sources").keyName("edge:many");
        for (int i = 1; i <= 25; i++) {
            many.source("edge:many/variant_" + i, f.between(0.2f, 1.0f), f.between(0.5f, 2.0f), f.between(1, 10),
                    false, 16, false, "file");
        }
        many.save();

        for (int i = 1; i <= BULK_SIZE; i++) {
            var sound = w.sound(p, bulkKey("sound", i))
                    .uiName(f.faker().lorem().word() + " " + f.faker().lorem().word())
                    .keyName("edge:bulk." + i).replace(f.chance(0.2))
                    .subTitle(f.chance(0.5) ? f.faker().lorem().sentence(3) : null);
            int sources = f.between(1, 3);
            for (int s = 0; s < sources; s++) {
                String type = f.pick(SOUND_SOURCE_TYPES);
                sound.source("event".equals(type) ? f.pick(SOUND_EVENTS) : "edge:bulk/" + i + "_" + s,
                        f.between(0.1f, 1.0f), f.between(0.5f, 2.0f), f.between(1, 5), f.chance(0.1),
                        f.between(8, 64), f.chance(0.2), type);
            }
            sound.save();
        }
    }

    private static void seedDimensions(SeedWriter w, Filler f, ProjectEntity p) {
        w.dimension(p, "no_children").uiName("No Attributes or Timelines").save();

        // Every environment attribute key once (keys are unique per dimension). There are more keys than
        // operators, so cycling through the operators covers each of them as well.
        var all = w.dimension(p, "all_attributes").uiName("All Environment Attributes")
                .timelines(TIMELINES.toArray(String[]::new));
        AttributeOperator[] operators = AttributeOperator.values();
        EnvironmentAttributeKey[] keys = EnvironmentAttributeKey.values();
        for (int i = 0; i < keys.length; i++) {
            all.attribute(keys[i], operators[i % operators.length], MinecraftCatalog.environmentValue(keys[i]));
        }
        all.save();

        // DTO bounds, spread over records that stay plausible for vanilla (minY/height multiples of 16,
        // minY + height <= 2032).
        w.dimension(p, "bounds_min")
                .uiName("Bounds: minimum").bounds(-2032, 16).coordinateScale(0.00001).monsterSpawn("constant:0", 0)
                .save();
        w.dimension(p, "bounds_max_height")
                .uiName("Bounds: maximum height").bounds(-2032, 4064).coordinateScale(30_000_000.0)
                .monsterSpawn("constant:15", 15)
                .save();
        w.dimension(p, "bounds_max_min_y_invalid_for_vanilla")
                .uiName("Bounds: maximum minY (invalid for vanilla)").bounds(2031, 16)
                .save();

        for (Skybox skybox : Skybox.values()) {
            for (CardinalLight light : CardinalLight.values()) {
                String key = "sky_" + skybox.name().toLowerCase(Locale.ROOT) + "_" + light.name().toLowerCase(Locale.ROOT);
                w.dimension(p, key).uiName("Skybox " + skybox + " / " + light).sky(skybox, light).save();
            }
        }
        w.dimension(p, "end_like")
                .uiName(UNICODE).flags(true, false, false, true).bounds(0, 256)
                .infiniburn(INFINIBURN_END).ambientLight(1.0f).sky(Skybox.END, CardinalLight.DEFAULT)
                .defaultClock(CLOCK_END)
                .save();

        for (int i = 1; i <= BULK_SIZE; i++) {
            int height = 16 * f.between(4, 24);
            var dimension = w.dimension(p, bulkKey("dimension", i))
                    .uiName(f.faker().ancient().primordial() + " Realm")
                    .flags(f.chance(0.3), f.chance(0.7), f.chance(0.3), f.chance(0.05))
                    .coordinateScale(f.pick(List.of(0.125, 1.0, 8.0)))
                    .bounds(-16 * f.between(0, 8), height)
                    .ambientLight(f.between(0.0f, 1.0f))
                    .monsterSpawn("constant:" + f.between(0, 15), f.between(0, 15))
                    .sky(f.pick(List.of(Skybox.values())), f.pick(List.of(CardinalLight.values())));
            if (f.chance(0.5)) {
                EnvironmentAttributeKey key = f.pick(List.of(EnvironmentAttributeKey.values()));
                dimension.attribute(key, f.pick(List.of(AttributeOperator.values())),
                        MinecraftCatalog.environmentValue(key));
            }
            if (f.chance(0.5)) {
                dimension.timelines(f.pick(TIMELINES));
            }
            dimension.save();
        }
    }

    private static void seedAttributes(SeedWriter w, Filler f, ProjectEntity p) {
        w.attribute(p, "bounds_min", "Bounds: minimum", 0, Double.MIN_VALUE);
        w.attribute(p, "bounds_large", "Bounds: large", 1_000_000_000, Double.MAX_VALUE);
        w.attribute(p, "long_texts", longText("Long attribute name "), 0.5, 1);
        w.attribute(p, "unicode", UNICODE, 1, 2);

        for (int i = 1; i <= BULK_SIZE; i++) {
            double max = f.between(1, 1000);
            w.attribute(p, bulkKey("attribute", i), f.faker().lorem().word() + " " + f.faker().lorem().word(),
                    f.between(0, (int) max), max);
        }
    }

    private static void seedNotifications(SeedWriter w, Filler f, ProjectEntity p) {
        for (String frameType : FRAME_TYPES) {
            w.notification(p, "frame_" + frameType, "Frame " + frameType, "§e" + frameType + " frame",
                    "minecraft:oak_sign", frameType, null);
        }
        w.notification(p, "long_texts", longText("Long notification name "), longText("§6§lLong title "),
                "minecraft:writable_book", "goal", longText("Long comment "));
        w.notification(p, "unicode", UNICODE, "§b" + UNICODE, "minecraft:amethyst_shard", "challenge", UNICODE);

        for (int i = 1; i <= BULK_SIZE; i++) {
            w.notification(p, bulkKey("notification", i), f.faker().lorem().sentence(2),
                    "§" + f.pick(COLOR_CODES) + f.faker().lorem().sentence(4), f.pick(MATERIALS),
                    f.pick(FRAME_TYPES), f.chance(0.5) ? f.faker().lorem().sentence() : null);
        }
    }

    private static final List<String> COLOR_CODES = List.of(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");

    private static String bulkKey(String type, int index) {
        return String.format(Locale.ROOT, "bulk_%s_%03d", type, index);
    }

    /**
     * Repeats {@code seed} until the text is exactly {@value #MAX_TEXT} characters long.
     */
    private static String longText(String seed) {
        return seed.repeat(MAX_TEXT / seed.length() + 1).substring(0, MAX_TEXT);
    }

    private static String[] numbered(String format, int count, Filler f) {
        String[] lines = new String[count];
        for (int i = 0; i < count; i++) {
            lines[i] = String.format(Locale.ROOT, format, i + 1) + f.faker().lorem().sentence(5);
        }
        return lines;
    }

    private static String[] privateUseRows(int rows, int width, int start) {
        String[] lines = new String[rows];
        for (int row = 0; row < rows; row++) {
            StringBuilder line = new StringBuilder(width);
            for (int column = 0; column < width; column++) {
                line.append((char) (start + row * width + column));
            }
            lines[row] = line.toString();
        }
        return lines;
    }
}
