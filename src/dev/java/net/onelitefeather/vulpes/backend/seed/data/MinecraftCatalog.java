package net.onelitefeather.vulpes.backend.seed.data;

import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;

import java.util.List;

/**
 * Curated vanilla Minecraft identifiers used by the seed data.
 *
 * <p>Everything here is a real identifier (1.21.x), so the UI shows values a user would actually
 * enter and downstream tooling (e.g. the generator) can resolve them.
 */
public final class MinecraftCatalog {

    public record Enchantment(String key, int maxLevel) {
    }

    public static final List<String> MATERIALS = List.of(
            "minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:iron_sword", "minecraft:golden_sword",
            "minecraft:diamond_sword", "minecraft:netherite_sword", "minecraft:mace", "minecraft:trident",
            "minecraft:bow", "minecraft:crossbow", "minecraft:shield", "minecraft:iron_axe", "minecraft:diamond_pickaxe",
            "minecraft:netherite_pickaxe", "minecraft:iron_shovel", "minecraft:golden_hoe",
            "minecraft:leather_helmet", "minecraft:chainmail_chestplate", "minecraft:iron_leggings",
            "minecraft:diamond_boots", "minecraft:netherite_helmet", "minecraft:turtle_helmet", "minecraft:elytra",
            "minecraft:potion", "minecraft:splash_potion", "minecraft:golden_apple", "minecraft:enchanted_golden_apple",
            "minecraft:cooked_beef", "minecraft:bread", "minecraft:honey_bottle",
            "minecraft:paper", "minecraft:book", "minecraft:writable_book", "minecraft:map", "minecraft:compass",
            "minecraft:clock", "minecraft:name_tag", "minecraft:gold_nugget", "minecraft:emerald", "minecraft:diamond",
            "minecraft:amethyst_shard", "minecraft:echo_shard", "minecraft:nether_star", "minecraft:heart_of_the_sea",
            "minecraft:blaze_rod", "minecraft:stick", "minecraft:totem_of_undying", "minecraft:ender_pearl",
            "minecraft:experience_bottle", "minecraft:recovery_compass"
    );

    public static final List<Enchantment> ENCHANTMENTS = List.of(
            new Enchantment("minecraft:sharpness", 5),
            new Enchantment("minecraft:smite", 5),
            new Enchantment("minecraft:bane_of_arthropods", 5),
            new Enchantment("minecraft:knockback", 2),
            new Enchantment("minecraft:fire_aspect", 2),
            new Enchantment("minecraft:looting", 3),
            new Enchantment("minecraft:sweeping_edge", 3),
            new Enchantment("minecraft:efficiency", 5),
            new Enchantment("minecraft:silk_touch", 1),
            new Enchantment("minecraft:fortune", 3),
            new Enchantment("minecraft:unbreaking", 3),
            new Enchantment("minecraft:mending", 1),
            new Enchantment("minecraft:power", 5),
            new Enchantment("minecraft:punch", 2),
            new Enchantment("minecraft:flame", 1),
            new Enchantment("minecraft:infinity", 1),
            new Enchantment("minecraft:multishot", 1),
            new Enchantment("minecraft:piercing", 4),
            new Enchantment("minecraft:quick_charge", 3),
            new Enchantment("minecraft:protection", 4),
            new Enchantment("minecraft:fire_protection", 4),
            new Enchantment("minecraft:blast_protection", 4),
            new Enchantment("minecraft:projectile_protection", 4),
            new Enchantment("minecraft:feather_falling", 4),
            new Enchantment("minecraft:respiration", 3),
            new Enchantment("minecraft:aqua_affinity", 1),
            new Enchantment("minecraft:thorns", 3),
            new Enchantment("minecraft:depth_strider", 3),
            new Enchantment("minecraft:frost_walker", 2),
            new Enchantment("minecraft:soul_speed", 3),
            new Enchantment("minecraft:swift_sneak", 3),
            new Enchantment("minecraft:loyalty", 3),
            new Enchantment("minecraft:riptide", 3),
            new Enchantment("minecraft:channeling", 1),
            new Enchantment("minecraft:impaling", 5),
            new Enchantment("minecraft:density", 5),
            new Enchantment("minecraft:breach", 4),
            new Enchantment("minecraft:wind_burst", 3),
            new Enchantment("minecraft:binding_curse", 1),
            new Enchantment("minecraft:vanishing_curse", 1)
    );

    /**
     * Item flag names as used by the Paper/Bukkit {@code ItemFlag} API.
     */
    public static final List<String> ITEM_FLAGS = List.of(
            "HIDE_ENCHANTS", "HIDE_ATTRIBUTES", "HIDE_UNBREAKABLE", "HIDE_DESTROYS", "HIDE_PLACED_ON",
            "HIDE_ADDITIONAL_TOOLTIP", "HIDE_DYE", "HIDE_ARMOR_TRIM", "HIDE_STORED_ENCHANTS"
    );

    public static final List<String> ITEM_GROUPS = List.of(
            "weapon", "armor", "tool", "consumable", "quest", "currency", "cosmetic", "misc"
    );

    /**
     * Advancement frame types (matched case-insensitively by the generator).
     */
    public static final List<String> FRAME_TYPES = List.of("task", "goal", "challenge");

    public static final List<String> SOUND_EVENTS = List.of(
            "minecraft:entity.player.levelup", "minecraft:entity.experience_orb.pickup",
            "minecraft:ui.button.click", "minecraft:ui.toast.challenge_complete", "minecraft:block.note_block.pling",
            "minecraft:block.anvil.use", "minecraft:block.chest.open", "minecraft:block.enchantment_table.use",
            "minecraft:block.beacon.activate", "minecraft:block.portal.trigger", "minecraft:entity.ender_dragon.growl",
            "minecraft:entity.wither.spawn", "minecraft:entity.villager.yes", "minecraft:entity.villager.no",
            "minecraft:item.totem.use", "minecraft:item.trident.thunder", "minecraft:ambient.cave",
            "minecraft:ambient.basalt_deltas.mood", "minecraft:music.nether.soul_sand_valley", "minecraft:weather.rain"
    );

    /**
     * Sound file source types as defined by the vanilla {@code sounds.json} format.
     */
    public static final List<String> SOUND_SOURCE_TYPES = List.of("file", "event");

    public static final String INFINIBURN_OVERWORLD = "#minecraft:infiniburn_overworld";
    public static final String INFINIBURN_NETHER = "#minecraft:infiniburn_nether";
    public static final String INFINIBURN_END = "#minecraft:infiniburn_end";

    public static final String CLOCK_OVERWORLD = "minecraft:overworld";
    public static final String CLOCK_END = "minecraft:the_end";

    public static final List<String> TIMELINES = List.of(
            "minecraft:day", "minecraft:early_game", "minecraft:moon", "minecraft:villager_schedule"
    );

    /**
     * A plausible serialized value for an environment attribute.
     *
     * <p>Unknown (future) keys fall back to {@code "0"} so that enum coverage keeps working when the
     * model gains new constants.
     */
    public static String environmentValue(EnvironmentAttributeKey key) {
        String name = key.name();
        if (name.endsWith("_COLOR") || name.equals("BLOCK_LIGHT_TINT")) {
            return "#" + Integer.toHexString(0x1000000 | (name.hashCode() & 0xFFFFFF)).substring(1).toUpperCase();
        }
        return switch (name) {
            case "FOG_START_DISTANCE", "WATER_FOG_START_DISTANCE" -> "0.0";
            case "FOG_END_DISTANCE", "SKY_FOG_END_DISTANCE", "CLOUD_FOG_END_DISTANCE" -> "192.0";
            case "WATER_FOG_END_DISTANCE" -> "96.0";
            case "CLOUD_HEIGHT" -> "192.33";
            case "SUN_ANGLE", "MOON_ANGLE", "STAR_ANGLE" -> "0.25";
            case "MOON_PHASE" -> "full_moon";
            case "STAR_BRIGHTNESS", "SKY_LIGHT_FACTOR", "MUSIC_VOLUME" -> "1.0";
            case "SKY_LIGHT_LEVEL" -> "15";
            case "DEFAULT_DRIPSTONE_PARTICLE" -> "{\"type\":\"minecraft:dripping_dripstone_water\"}";
            case "AMBIENT_PARTICLES" ->
                    "[{\"particle\":{\"type\":\"minecraft:white_ash\"},\"probability\":0.004}]";
            case "BACKGROUND_MUSIC" -> "{\"default\":{\"sound\":\"minecraft:music.nether.basalt_deltas\"}}";
            case "AMBIENT_SOUNDS" -> "{\"loop\":\"minecraft:ambient.basalt_deltas.loop\"}";
            case "FIREFLY_BUSH_SOUNDS" -> "minecraft:block.firefly_bush.idle";
            case "BED_RULE" -> "{\"can_sleep\":\"never\",\"can_set_spawn\":\"never\",\"explodes\":true}";
            case "TURTLE_EGG_HATCH_CHANCE", "SURFACE_SLIME_SPAWN_CHANCE", "CAT_WAKING_UP_GIFT_CHANCE" -> "0.5";
            case "VILLAGER_ACTIVITY", "BABY_VILLAGER_ACTIVITY" -> "minecraft:idle";
            case "CAN_START_RAID", "WATER_EVAPORATES", "RESPAWN_ANCHOR_WORKS", "NETHER_PORTAL_SPAWNS_PIGLINS",
                 "FAST_LAVA", "INCREASED_FIRE_BURNOUT", "EYEBLOSSOM_OPEN", "PIGLINS_ZOMBIFY", "SNOW_GOLEM_MELTS",
                 "CREAKING_ACTIVE", "BEES_STAY_IN_HIVE", "MONSTERS_BURN", "CAN_PILLAGER_PATROL_SPAWN" -> "true";
            default -> "0";
        };
    }

    private MinecraftCatalog() {
    }
}
