package net.onelitefeather.vulpes.backend.seed.fixture;

import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.seed.SeedWriter;
import net.onelitefeather.vulpes.backend.seed.data.Filler;

import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.CLOCK_OVERWORLD;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.INFINIBURN_NETHER;

/**
 * "Eldoria RPG": a fantasy RPG server as it would be set up in Vulpes. Every entity type is present
 * and every aggregate has children.
 *
 * <p>Keys marked "shared" also exist in {@link SkyblockFixture} to make project scoping visible.
 */
public final class EldoriaFixture implements ProjectFixture {

    public static final String PROJECT_KEY = "eldoria_rpg";

    @Override
    public void seed(SeedWriter w, Filler filler) {
        ProjectEntity p = w.project(PROJECT_KEY, "Eldoria RPG",
                "Fantasy RPG server with quests, guilds and a shadow realm.", false,
                "https://eldoria.example.net", "https://docs.eldoria.example.net");

        seedItems(w, p);
        seedFonts(w, p);
        seedSounds(w, p);
        seedDimensions(w, p);
        seedAttributes(w, p);
        seedNotifications(w, p);
    }

    private static void seedItems(SeedWriter w, ProjectEntity p) {
        w.item(p, "starter_sword") // shared
                .uiName("Starter Sword").displayName("§fRusty Blade").material("minecraft:iron_sword")
                .group("weapon").customModelData(1000)
                .lore("§7Every hero starts somewhere.", "§8Soulbound")
                .flags("HIDE_ATTRIBUTES")
                .save();
        w.item(p, "frostmourne")
                .uiName("Frostmourne").displayName("§b§lFrostmourne").material("minecraft:netherite_sword")
                .group("weapon").customModelData(1001).comment("Legendary drop of the Lich King raid")
                .enchant("minecraft:sharpness", 5).enchant("minecraft:unbreaking", 3).enchant("minecraft:mending", 1)
                .lore("§3Whomsoever takes up this blade", "§3shall wield power eternal.", "§r",
                        "§6Legendary", "§7Requires level §e60")
                .flags("HIDE_ENCHANTS", "HIDE_ATTRIBUTES")
                .save();
        w.item(p, "stormcaller_bow")
                .uiName("Stormcaller").displayName("§eStormcaller").material("minecraft:bow")
                .group("weapon").customModelData(1002)
                .enchant("minecraft:power", 5).enchant("minecraft:flame", 1).enchant("minecraft:infinity", 1)
                .lore("§7Arrows crackle with lightning.", "§5Epic")
                .flags("HIDE_ENCHANTS")
                .save();
        w.item(p, "aegis_of_dawn")
                .uiName("Aegis of Dawn").displayName("§6Aegis of Dawn").material("minecraft:shield")
                .group("armor").customModelData(1003)
                .enchant("minecraft:unbreaking", 3)
                .lore("§7Forged in the first light of Eldoria.", "§9Rare")
                .save();
        w.item(p, "wardens_helm")
                .uiName("Warden's Helm").displayName("§8Warden's Helm").material("minecraft:netherite_helmet")
                .group("armor").customModelData(1004)
                .enchant("minecraft:protection", 4).enchant("minecraft:respiration", 3)
                .enchant("minecraft:aqua_affinity", 1)
                .lore("§7Worn by the guardians of the deep halls.", "§5Epic")
                .flags("HIDE_ENCHANTS", "HIDE_ARMOR_TRIM")
                .save();
        w.item(p, "traveler_boots")
                .uiName("Traveler's Boots").displayName("§aTraveler's Boots").material("minecraft:leather_boots")
                .group("armor").customModelData(1005)
                .enchant("minecraft:feather_falling", 4)
                .lore("§7Light as a feather, dyed forest green.")
                .flags("HIDE_DYE")
                .save();
        w.item(p, "ember_pickaxe")
                .uiName("Ember Pickaxe").displayName("§cEmber Pickaxe").material("minecraft:diamond_pickaxe")
                .group("tool").customModelData(1006)
                .enchant("minecraft:efficiency", 5).enchant("minecraft:fortune", 3)
                .lore("§7Smelts ore as it mines.")
                .save();
        w.item(p, "healing_draught")
                .uiName("Healing Draught").displayName("§dHealing Draught").material("minecraft:potion")
                .group("consumable").customModelData(1007).amount(3)
                .lore("§7Restores §c6 ❤§7 over 5 seconds.")
                .flags("HIDE_ADDITIONAL_TOOLTIP")
                .save();
        w.item(p, "quest_scroll")
                .uiName("Quest Scroll").displayName("§fSealed Quest Scroll").material("minecraft:paper")
                .group("quest").customModelData(1008)
                .lore("§7Deliver to §eElder Maren§7 in Dawnhold.")
                .save();
        w.item(p, "guild_coin")
                .uiName("Guild Coin").displayName("§6Guild Coin").material("minecraft:gold_nugget")
                .group("currency").customModelData(1009).amount(64)
                .lore("§7Accepted by every guild merchant.")
                .save();
    }

    private static void seedFonts(SeedWriter w, ProjectEntity p) {
        w.font(p, "hud_icons") // shared
                .uiName("HUD Icons").texture("eldoria:font/hud_icons.png").metrics(8, 9)
                .comment("Health, mana and stamina icons for the action bar")
                .chars("", "")
                .save();
        w.font(p, "rune_script")
                .uiName("Rune Script").texture("eldoria:font/runes.png").metrics(7, 8)
                .comment("Ancient runes used on quest scrolls")
                .chars("",
                        "",
                        "")
                .save();
        w.font(p, "npc_portraits")
                .uiName("NPC Portraits").texture("eldoria:font/portraits.png").metrics(32, 32)
                .comment("Dialog portraits")
                .chars("")
                .save();
    }

    private static void seedSounds(SeedWriter w, ProjectEntity p) {
        w.sound(p, "ui_click") // shared
                .uiName("UI Click").keyName("eldoria:ui.click").subTitle("Button clicked")
                .source("minecraft:ui.button.click", 0.6f, 1.2f, 1, false, 16, true, "event")
                .save();
        w.sound(p, "quest_complete")
                .uiName("Quest Complete").keyName("eldoria:quest.complete").subTitle("Quest completed")
                .file("eldoria:quest/complete_1")
                .file("eldoria:quest/complete_2")
                .save();
        w.sound(p, "shadow_realm_ambience")
                .uiName("Shadow Realm Ambience").keyName("eldoria:ambient.shadow_realm").replace(true)
                .subTitle("Whispers echo")
                .source("eldoria:ambient/shadow_loop", 0.4f, 0.8f, 1, true, 48, false, "file")
                .source("eldoria:ambient/whisper_1", 0.7f, 1.0f, 3, false, 24, false, "file")
                .source("eldoria:ambient/whisper_2", 0.7f, 0.9f, 2, false, 24, false, "file")
                .save();
        w.sound(p, "boss_roar")
                .uiName("Boss Roar").keyName("eldoria:entity.lich_king.roar").subTitle("The Lich King roars")
                .source("minecraft:entity.ender_dragon.growl", 1.0f, 0.5f, 1, false, 64, true, "event")
                .save();
    }

    private static void seedDimensions(SeedWriter w, ProjectEntity p) {
        w.dimension(p, "lobby") // shared
                .uiName("Dawnhold (Lobby)").flags(true, true, false, false)
                .timelines("minecraft:day")
                .attribute(EnvironmentAttributeKey.SKY_COLOR, "#78A7FF")
                .save();
        w.dimension(p, "shadow_realm")
                .uiName("Shadow Realm").flags(true, false, true, false)
                .coordinateScale(8.0).bounds(0, 256).logicalHeight(128)
                .infiniburn(INFINIBURN_NETHER).ambientLight(0.1f)
                .monsterSpawn("constant:7", 15).sky(Skybox.NONE, CardinalLight.NETHER)
                .defaultClock(CLOCK_OVERWORLD)
                .attribute(EnvironmentAttributeKey.FOG_COLOR, "#330808")
                .attribute(EnvironmentAttributeKey.FOG_END_DISTANCE, AttributeOperator.MULTIPLY, "0.5")
                .attribute(EnvironmentAttributeKey.AMBIENT_PARTICLES,
                        "[{\"particle\":{\"type\":\"minecraft:white_ash\"},\"probability\":0.004}]")
                .attribute(EnvironmentAttributeKey.BACKGROUND_MUSIC,
                        "{\"default\":{\"sound\":\"minecraft:music.nether.soul_sand_valley\"}}")
                .attribute(EnvironmentAttributeKey.WATER_EVAPORATES, "true")
                .attribute(EnvironmentAttributeKey.MONSTERS_BURN, "false")
                .timelines("minecraft:moon", "minecraft:villager_schedule")
                .save();
    }

    private static void seedAttributes(SeedWriter w, ProjectEntity p) {
        w.attribute(p, "bonus_health", "Bonus Health", 0, 40); // shared
        w.attribute(p, "max_mana", "Max Mana", 100, 1000);
        w.attribute(p, "mana_regen", "Mana Regeneration", 1.5, 20);
        w.attribute(p, "stamina", "Stamina", 50, 200);
        w.attribute(p, "crit_chance", "Critical Hit Chance", 0.05, 1);
    }

    private static void seedNotifications(SeedWriter w, ProjectEntity p) {
        w.notification(p, "welcome", "Welcome", "§6Welcome to Eldoria!", "minecraft:compass", "task",
                "Shown on first join"); // shared
        w.notification(p, "quest_complete", "Quest Complete", "§aQuest complete!", "minecraft:writable_book",
                "goal", "Shown after turning in a quest");
        w.notification(p, "level_up", "Level Up", "§eLevel up!", "minecraft:experience_bottle", "task", null);
        w.notification(p, "lich_king_slain", "Lich King Slain", "§5The Lich King has fallen", "minecraft:nether_star",
                "challenge", "Server-wide broadcast after the raid");
    }
}
