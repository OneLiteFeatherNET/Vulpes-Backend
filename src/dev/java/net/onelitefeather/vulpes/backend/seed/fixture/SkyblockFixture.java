package net.onelitefeather.vulpes.backend.seed.fixture;

import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.seed.SeedWriter;
import net.onelitefeather.vulpes.backend.seed.data.Filler;

import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.CLOCK_END;
import static net.onelitefeather.vulpes.backend.seed.data.MinecraftCatalog.INFINIBURN_END;

/**
 * "Skyblock Lab": a smaller experimental ({@code labor}) project. Shares one key per entity type with
 * {@link EldoriaFixture}, with different content.
 */
public final class SkyblockFixture implements ProjectFixture {

    public static final String PROJECT_KEY = "skyblock_lab";

    @Override
    public void seed(SeedWriter w, Filler filler) {
        ProjectEntity p = w.project(PROJECT_KEY, "Skyblock Lab",
                "Experimental skyblock mode: islands, generators and minions.", true,
                "https://skyblock.example.net", null);

        w.item(p, "starter_sword") // shared
                .uiName("Wooden Starter Sword").displayName("§7Wooden Sword").material("minecraft:wooden_sword")
                .group("weapon")
                .lore("§7Found in the starter chest.")
                .save();
        w.item(p, "island_upgrade_token")
                .uiName("Island Upgrade Token").displayName("§bIsland Upgrade Token").material("minecraft:heart_of_the_sea")
                .group("currency").customModelData(3001)
                .enchant("minecraft:unbreaking", 1)
                .lore("§7Right-click to expand your island.", "§8Tier II")
                .flags("HIDE_ENCHANTS")
                .save();
        w.item(p, "cobble_minion")
                .uiName("Cobblestone Minion").displayName("§aCobblestone Minion I").material("minecraft:clock")
                .group("tool").customModelData(3002)
                .lore("§7Mines cobblestone every §e14s§7.", "§7Storage: §e64")
                .save();

        w.font(p, "hud_icons") // shared
                .uiName("Island HUD").texture("skyblock:font/island_hud.png").metrics(9, 10)
                .comment("Coins and island level icons")
                .chars("")
                .save();

        w.sound(p, "ui_click") // shared
                .uiName("Soft Click").keyName("skyblock:ui.click").subTitle("Click")
                .file("skyblock:ui/soft_click")
                .save();
        w.sound(p, "minion_collect")
                .uiName("Minion Collect").keyName("skyblock:minion.collect")
                .source("minecraft:entity.experience_orb.pickup", 0.5f, 1.6f, 1, false, 8, false, "event")
                .save();

        w.dimension(p, "lobby") // shared
                .uiName("Void Lobby").flags(true, true, false, false)
                .bounds(0, 256).infiniburn(INFINIBURN_END).sky(Skybox.END, CardinalLight.DEFAULT)
                .defaultClock(CLOCK_END)
                .attribute(EnvironmentAttributeKey.CLOUD_HEIGHT, "-64.0")
                .timelines("minecraft:early_game")
                .save();

        w.attribute(p, "bonus_health", "Island Health Bonus", 4, 20); // shared
        w.attribute(p, "minion_speed", "Minion Speed", 1, 5);

        w.notification(p, "welcome", "Island Welcome", "§bYour island awaits!", "minecraft:grass_block", "task",
                "Shown after creating an island"); // shared
    }
}
