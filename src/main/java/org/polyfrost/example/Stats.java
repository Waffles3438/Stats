package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.common.MinecraftForge;
import org.polyfrost.example.command.BedwarsStatsCommand;
import org.polyfrost.example.command.DuelsStatsCommand;
import org.polyfrost.example.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.polyfrost.example.util.Bedwars;
import org.polyfrost.example.util.Duels;
import org.polyfrost.example.util.EldestRemovalMap;
import org.polyfrost.example.util.Ranks;

@Mod(modid = Stats.MODID, name = Stats.NAME, version = Stats.VERSION)
public class Stats {

    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";
    public static ModConfig config;
    public static int maxSize = 16;
    public static EldestRemovalMap<String, Duels> duelsStatsList = new EldestRemovalMap<>(maxSize);
    public static EldestRemovalMap<String, Bedwars> bedwarsStatsList = new EldestRemovalMap<>(maxSize);
    public static EldestRemovalMap<String, Ranks> playerRanks = new EldestRemovalMap<>(maxSize);

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new ModConfig();
        CommandManager.INSTANCE.registerCommand(new BedwarsStatsCommand());
        CommandManager.INSTANCE.registerCommand(new DuelsStatsCommand());
    }
}
