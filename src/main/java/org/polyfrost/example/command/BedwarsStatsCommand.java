package org.polyfrost.example.command;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import org.polyfrost.example.util.Bedwars;
import org.polyfrost.example.util.HypixelApiClient;
import org.polyfrost.example.util.PlayerProfile;

import java.util.UUID;

@Command(value = "bw")
public class BedwarsStatsCommand {
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
            printStats(playerName, result.getProfile(), result.getBedwars());
        });
    }

    private void printStats(String requestedName, PlayerProfile profile, Bedwars stats) {
        if (profile.getDisplayName() == null) {
            UChat.chat(requestedName + " has no Hypixel stats.");
            return;
        }
        if (stats.getBedwarsStar() == -1) {
            UChat.chat(requestedName + " has never played Bedwars");
            return;
        }
        String formattedName = formatName(profile);
        UChat.chat("§9------------------------------------------");
        UChat.chat(getFormattedRank(stats.getBedwarsStar()) + " " + formattedName + " " + profile.getGuildTag());
        UChat.chat("FKDR: " + formatColors(stats.getBedwarsFKDR(), 15));
        UChat.chat("Final kills: " + formatColors(stats.getBedwarsFinalKills(), 25000));
        UChat.chat("WLR: " + formatColors(stats.getBedwarsWLR(), 5));
        UChat.chat("Wins: " + formatColors(stats.getBedwarsWins(), 20000));
        UChat.chat("BBLR: " + formatColors(stats.getBedwarsBBLR(), 5));
        UChat.chat("Beds: " + formatColors(stats.getBedwarsBedBreaks(), 30000));
        if (stats.getBedwarsWinStreak() != -1) UChat.chat("Winstreak: " + stats.getBedwarsWinStreak());
        UChat.chat("§9------------------------------------------");
    }

    static String formatName(PlayerProfile profile) {
        String rank = profile.getRank();
        return rank == null || rank.isEmpty() ? profile.getDisplayName() : rank + " " + profile.getDisplayName();
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

    private enum Rank {
        STONE1("§7[_✫]"), STONE("§7[__✫]"), IRON("§f[___✫]"), GOLD("§6[___✫]"), DIAMOND("§b[___✫]"),
        EMERALD("§2[___✫]"), SAPPHIRE("§3[___✫]"), RUBY("§4[___✫]"), CRYSTAL("§d[___✫]"), OPAL("§9[___✫]"),
        AMETHYST("§5[___✫]"), RAINBOW("§c[§6_§e_§a_§b_§d✫§5]"), IRON_PRIME("§7[§f____§7✪§7]"),
        GOLD_PRIME("§7[§e____§6✪§7]"), DIAMOND_PRIME("§7[§b____§3✪§7]"), EMERALD_PRIME("§7[§a____§2✪§7]"),
        SAPPHIRE_PRIME("§7[§3____§9✪§7]"), RUBY_PRIME("§7[§c____§4✪§7]"), CRYSTAL_PRIME("§7[§d____§5✪§7]"),
        OPAL_PRIME("§7[§9____§1✪§7]"), AMETHYST_PRIME("§7[§5____§8✪§7]"), MIRROR("§8[§7_§f__§7_§8✪]"),
        LIGHT("§f[_§e__§6_§l⚝§6]"), DAWN("§6[_§f__§b_§3§l⚝§3]"), DUSK("§5[_§d__§6_§e§l⚝§e]"),
        AIR("§b[_§f__§7_§l⚝§8]"), WIND("§f[_§a__§2_§l⚝§2]"), NEBULA("§4[_§c__§d_§l⚝§d]"),
        THUNDER("§e[_§f__§8_§l⚝§8]"), EARTH("§a[_§2__§6_§l⚝§e]"), WATER("§b[_§3__§9_§l⚝§1]"),
        FIRE("§e[_§6__§c_§l⚝§4]"), THREEONE("§9[_§3__§60✥§e]"), THREETWO("§c[§4_§7__§4_§c✥]"),
        THREETHREE("§9[__§d_§6_✥§d]"), THREEFOUR("§2[_§d__§5_✥§2]"), THREEFIVE("§c[_§4__§2_§a✥]"),
        THREESIX("§a[__§b_§9_§1✥]"), THREESEVEN("§4[_§c__§b_§3✥]"), THREEEIGHT("§1[_§9_§5__§d✥§1]"),
        THREENINE("§c[_§a__§3_§9✥]"), FOURZERO("§5[_§c__§6_✥§e]"), FOURONE("§e[_§6_§c_§d_✥§5]"),
        FOURTWO("§1[§9_§3_§b_§f_§7✥]"), FOURTHREE("§0[§5_§8__§5_✥§0]"), FOURFOUR("§2[_§a_§e_§6_§5✥§d]"),
        FOURFIVE("§f[_§b__§3_✥]"), FOURSIX("§3[§b_§e__§6_§d✥§5]"), FOURSEVEN("§f[§4_§c__§9_§1✥§9]"),
        FOUREIGHT("§5[_§c_§6_§e_§b✥§3]"), FOURNINE("§2[§a_§f__§a_✥§2]"), FIVEZERO("§4[_§5_§9__§1✥§0]");

        private final String format;
        Rank(String format) { this.format = format; }
    }

    public String getFormattedRank(int star) {
        String format = rankFor(star).format;
        String number = String.valueOf(star);
        StringBuilder result = new StringBuilder(format);
        int digit = 0;
        for (int index = 0; index < result.length(); index++) {
            if (result.charAt(index) == '_' && digit < number.length()) result.setCharAt(index, number.charAt(digit++));
        }
        return result.toString();
    }

    private Rank rankFor(int star) {
        if (star < 10) return Rank.STONE1;
        if (star < 100) return Rank.STONE;
        if (star < 200) return Rank.IRON;
        if (star < 300) return Rank.GOLD;
        if (star < 400) return Rank.DIAMOND;
        if (star < 500) return Rank.EMERALD;
        if (star < 600) return Rank.SAPPHIRE;
        if (star < 700) return Rank.RUBY;
        if (star < 800) return Rank.CRYSTAL;
        if (star < 900) return Rank.OPAL;
        if (star < 1000) return Rank.AMETHYST;
        if (star < 1100) return Rank.RAINBOW;
        if (star < 1200) return Rank.IRON_PRIME;
        if (star < 1300) return Rank.GOLD_PRIME;
        if (star < 1400) return Rank.DIAMOND_PRIME;
        if (star < 1500) return Rank.EMERALD_PRIME;
        if (star < 1600) return Rank.SAPPHIRE_PRIME;
        if (star < 1700) return Rank.RUBY_PRIME;
        if (star < 1800) return Rank.CRYSTAL_PRIME;
        if (star < 1900) return Rank.OPAL_PRIME;
        if (star < 2000) return Rank.AMETHYST_PRIME;
        if (star < 2100) return Rank.MIRROR;
        if (star < 2200) return Rank.LIGHT;
        if (star < 2300) return Rank.DAWN;
        if (star < 2400) return Rank.DUSK;
        if (star < 2500) return Rank.AIR;
        if (star < 2600) return Rank.WIND;
        if (star < 2700) return Rank.NEBULA;
        if (star < 2800) return Rank.THUNDER;
        if (star < 2900) return Rank.EARTH;
        if (star < 3000) return Rank.WATER;
        if (star < 3100) return Rank.FIRE;
        if (star < 3200) return Rank.THREEONE;
        if (star < 3300) return Rank.THREETWO;
        if (star < 3400) return Rank.THREETHREE;
        if (star < 3500) return Rank.THREEFOUR;
        if (star < 3600) return Rank.THREEFIVE;
        if (star < 3700) return Rank.THREESIX;
        if (star < 3800) return Rank.THREESEVEN;
        if (star < 3900) return Rank.THREEEIGHT;
        if (star < 4000) return Rank.THREENINE;
        if (star < 4100) return Rank.FOURZERO;
        if (star < 4200) return Rank.FOURONE;
        if (star < 4300) return Rank.FOURTWO;
        if (star < 4400) return Rank.FOURTHREE;
        if (star < 4500) return Rank.FOURFOUR;
        if (star < 4600) return Rank.FOURFIVE;
        if (star < 4700) return Rank.FOURSIX;
        if (star < 4800) return Rank.FOURSEVEN;
        if (star < 4900) return Rank.FOUREIGHT;
        if (star < 5000) return Rank.FOURNINE;
        return Rank.FIVEZERO;
    }
}
