package org.polyfrost.example;

import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.polyfrost.example.command.BedwarsStatsCommand;
import org.polyfrost.example.command.DuelsStatsCommand;
import org.polyfrost.example.config.ModConfig;

@Mod(modid = Stats.MODID, name = Stats.NAME, version = Stats.VERSION)
public class Stats {
    public static final String MODID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    public static ModConfig config;

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        config = new ModConfig();
        CommandManager.INSTANCE.registerCommand(new BedwarsStatsCommand());
        CommandManager.INSTANCE.registerCommand(new DuelsStatsCommand());
    }
}
