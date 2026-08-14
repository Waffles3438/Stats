package org.polyfrost.example.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Button;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.utils.Notifications;
import org.polyfrost.example.Stats;

public class ModConfig extends Config {
    @Text(
            name = "Hypixel API key",
            secure = true,
            multiline = false,
            subcategory = "Stat Checking"
    )
    public static String api = "";

    @Slider(
            name = "Amount of players cached (restart required)",
            min = 1,
            max = 16,
            step = 1,
            subcategory = "Stat Checking"
    )
    public static int maxCacheSize = 4;

    @Button(
            name = "Clear cache",
            text = "Clear",
            subcategory = "Stat Checking"
    )
    Runnable runnable = () -> {
        synchronized (Stats.CACHE_LOCK) {
            Stats.bedwarsStatsList.clear();
            Stats.duelsStatsList.clear();
            Stats.playerProfileList.clear();
        }
        Notifications.INSTANCE.send("Stats", "Cleared player cache", 3000);
    };

    public ModConfig() {
        super(new Mod(Stats.NAME, ModType.UTIL_QOL), Stats.MODID + ".json");
        initialize();
    }
}
