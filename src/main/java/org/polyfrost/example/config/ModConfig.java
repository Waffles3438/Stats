package org.polyfrost.example.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import org.polyfrost.example.Stats;

public class ModConfig extends Config {
    @Text(
            name = "Stats backend URL",
            secure = false,
            multiline = false,
            subcategory = "Stat Checking"
    )
    public static String backendUrl = "https://api.wafflestats.com";

    public ModConfig() {
        super(new Mod(Stats.NAME, ModType.UTIL_QOL), Stats.MODID + ".json");
        initialize();
    }
}
