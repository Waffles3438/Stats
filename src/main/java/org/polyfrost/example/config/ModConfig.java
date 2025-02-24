package org.polyfrost.example.config;

import cc.polyfrost.oneconfig.config.annotations.Text;
import org.polyfrost.example.Stats;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class ModConfig extends Config {
    @Text(
            name = "Hypixel API",
            secure = true, multiline = false
    )
    public static String api = "";

    public ModConfig() {
        super(new Mod(Stats.NAME, ModType.UTIL_QOL), Stats.MODID + ".json");
        initialize();
    }
}

