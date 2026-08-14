package org.polyfrost.example.command;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import org.polyfrost.example.util.Duels;
import org.polyfrost.example.util.HypixelApiClient;
import org.polyfrost.example.util.PlayerProfile;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

@Command(value = "d")
public class DuelsStatsCommand {
    @Main
    private void main() {
        GameProfile profile = Minecraft.getMinecraft().getSession().getProfile();
        lookupAndPrint(profile.getName(), profile.getId());
    }

    @Main
    private void main(GameProfile player) {
        lookupAndPrint(player.getName(), player.getId());
    }

    private void lookupAndPrint(final String playerName, final UUID uuid) {
        Multithreading.runAsync(() -> {
            HypixelApiClient.LookupResult result = HypixelApiClient.lookup(playerName, uuid);
            if (!result.isSuccess()) {
                UChat.chat(result.getError());
                return;
            }
            printStats(playerName, result.getProfile(), result.getDuels());
        });
    }

    private void printStats(String requestedName, PlayerProfile profile, Duels stats) {
        if (profile.getDisplayName() == null) {
            UChat.chat(requestedName + " has no Hypixel stats.");
            return;
        }
        if (stats.getDuelsDeaths() == -1) {
            UChat.chat(requestedName + " has never played Duels.");
            return;
        }
        UChat.chat("§9------------------------------------------");
        UChat.chat(getPlayerDivision(stats.getDuelsWins()) + BedwarsStatsCommand.formatName(profile) + " " + profile.getGuildTag());
        UChat.chat("Level: " + stats.getLevel());
        UChat.chat("WLR: " + formatColors(stats.getDuelsWLR(), 10));
        UChat.chat("Wins: " + formatColors(stats.getDuelsWins(), 20000));
        UChat.chat("KDR: " + formatColors(stats.getDuelsKDR(), 10));
        UChat.chat("Kills: " + formatColors(stats.getDuelsKills(), 20000));
        if (stats.getDuelsCWS() != -1 && stats.getDuelsBWS() != -1) {
            UChat.chat("Current Winstreak: " + stats.getDuelsCWS());
            UChat.chat("Best Winstreak: " + stats.getDuelsBWS());
        }
        UChat.chat("§9------------------------------------------");
    }

    private String formatColors(int stat, int god) { return formatColors((double) stat, god); }

    private String formatColors(double stat, int god) {
        if (stat > god * 2D) return "§0" + stat;
        if (stat >= god) return "§4" + stat;
        if (stat > god * .875D) return "§c" + stat;
        if (stat > god * .75D) return "§6" + stat;
        if (stat > god * .625D) return "§e" + stat;
        if (stat > god * .5D) return "§2" + stat;
        if (stat > god * .375D) return "§a" + stat;
        if (stat > god * .25D) return "§b" + stat;
        if (stat > god * .125D) return "§f" + stat;
        return "§7" + stat;
    }

    public static String levelColor(String level) {
        double value = Double.parseDouble(level);
        if (value < 35) return "§c" + level;
        if (value < 45) return "§6" + level;
        if (value < 55) return "§a" + level;
        if (value < 65) return "§e" + level;
        if (value < 75) return "§d" + level;
        if (value < 85) return "§f" + level;
        if (value < 95) return "§9" + level;
        if (value < 150) return "§2" + level;
        if (value < 200) return "§4" + level;
        if (value < 250) return "§5" + level;
        return "§0" + level;
    }

    private static final NavigableMap<Integer, String> DIVISIONS = new TreeMap<>();
    static {
        String[] names = {"§8Rookie", "§fIron", "§6Gold", "§3Diamond", "§2Master", "§4§lLegend", "§e§lGrandmaster", "§5§lGodlike", "§b§lCelestial", "§d§lDivine", "§c§lAscended"};
        int[][] thresholds = {
                {100, 120, 140, 160, 180}, {200, 260, 320, 380, 440}, {500, 600, 700, 800, 900},
                {1000, 1200, 1400, 1600, 1800}, {2000, 2400, 2800, 3200, 3600}, {4000, 5200, 6400, 7600, 8800},
                {10000, 12000, 14000, 16000, 18000}, {20000, 26000, 32000, 38000, 44000},
                {50000, 60000, 70000, 80000, 90000}, {100000, 120000, 140000, 160000, 180000}
        };
        for (int rank = 0; rank < thresholds.length; rank++) {
            for (int tier = 0; tier < thresholds[rank].length; tier++) {
                DIVISIONS.put(thresholds[rank][tier], names[rank] + (tier == 0 ? "" : " " + roman(tier + 1)));
            }
        }
        for (int tier = 1; tier <= 50; tier++) DIVISIONS.put(200000 + (tier - 1) * 20000, names[10] + " " + roman(tier));
    }

    public static String getPlayerDivision(int wins) {
        return wins < 100 ? "" : DIVISIONS.floorEntry(wins).getValue() + " ";
    }

    private static String roman(int value) {
        String[] numerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX", "XXX", "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX", "XL", "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX", "L"};
        return numerals[value];
    }
}
