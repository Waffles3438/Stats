package org.polyfrost.example.config;

import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.utils.Notifications;
import org.polyfrost.example.Stats;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class ModConfig extends Config {
    @Text(
            name = "Hypixel API",
            secure = true, multiline = false,
            subcategory = "Stat Checking"
    )
    public static String api = "";

    @Button(
            name = "Clear cache",
            text = "Clear",
            subcategory = "Stat Checking"
    )
    Runnable runnable = () -> {
        Stats.bedwarsStatsList.clear();
        Stats.duelsStatsList.clear();
        Stats.playerRanks.clear();
        Notifications.INSTANCE.send("Stats", "Cleared player cache", 1000);
    };

    public ModConfig() {
        super(new Mod(Stats.NAME, ModType.UTIL_QOL), Stats.MODID + ".json");
        initialize();
    }
}

