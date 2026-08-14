package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.polyfrost.example.command.BedwarsStatsCommand;
import org.polyfrost.example.command.DuelsStatsCommand;
import org.polyfrost.example.config.ModConfig;
import org.polyfrost.example.util.Bedwars;
import org.polyfrost.example.util.Duels;
import org.polyfrost.example.util.EldestRemovalMap;
import org.polyfrost.example.util.PlayerProfile;

@Mod(modid = Stats.MODID, name = Stats.NAME, version = Stats.VERSION)
public class Stats {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    /** Guards all LRU caches because lookups run asynchronously. */
    public static final Object CACHE_LOCK = new Object();
    public static ModConfig config;
    public static EldestRemovalMap<String, Duels> duelsStatsList;
    public static EldestRemovalMap<String, Bedwars> bedwarsStatsList;
    public static EldestRemovalMap<String, PlayerProfile> playerProfileList;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new ModConfig();
        synchronized (CACHE_LOCK) {
            duelsStatsList = new EldestRemovalMap<>(ModConfig.maxCacheSize);
            bedwarsStatsList = new EldestRemovalMap<>(ModConfig.maxCacheSize);
            playerProfileList = new EldestRemovalMap<>(ModConfig.maxCacheSize);
        }
        CommandManager.INSTANCE.registerCommand(new BedwarsStatsCommand());
        CommandManager.INSTANCE.registerCommand(new DuelsStatsCommand());
    }
}
