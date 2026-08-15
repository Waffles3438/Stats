package org.polyfrost.example.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.polyfrost.example.command.DuelsStatsCommand;
import org.polyfrost.example.config.ModConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * Fetches and parses stat data from the mod's configured backend.
 *
 * <p>The production backend owns the Hypixel API key. This client never receives or sends it.</p>
 */
public final class HypixelApiClient {
    private static final String STATS_ENDPOINT = "/v1/stats?uuid=";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final int MAX_ATTEMPTS = 3;

    private HypixelApiClient() {
    }

    public static LookupResult lookup(String playerName, UUID uuid) {
        if (uuid == null) {
            return LookupResult.error("Invalid player");
        }
        if (ModConfig.backendUrl == null || ModConfig.backendUrl.trim().isEmpty()) {
            return LookupResult.error("Set the Stats backend URL in the mod configuration before checking stats.");
        }

        final String endpoint;
        try {
            endpoint = statsEndpoint(uuid);
        } catch (IllegalArgumentException exception) {
            return LookupResult.error(exception.getMessage());
        }
        ApiResponse response = request(endpoint);
        if (!response.isSuccess()) {
            return LookupResult.error(response.error);
        }

        JsonObject player;
        JsonObject guild = new JsonObject();
        try {
            JsonObject root = new JsonParser().parse(response.body).getAsJsonObject();
            if (hasFalseSuccess(root)) {
                return LookupResult.error(apiCause(root, "Stats service rejected the request."));
            }
            if (!root.has("player") || root.get("player").isJsonNull() || !root.get("player").isJsonObject()) {
                return LookupResult.error(playerName + " has no Hypixel stats.");
            }
            if (!root.has("guild")) {
                return LookupResult.error("Stats service returned an incomplete response. Please try again.");
            }
            JsonElement guildElement = root.get("guild");
            if (!guildElement.isJsonNull() && !guildElement.isJsonObject()) {
                return LookupResult.error("Stats service returned an invalid response. Please try again.");
            }
            player = root.getAsJsonObject("player");
            if (guildElement.isJsonObject()) {
                guild = guildElement.getAsJsonObject();
            }
        } catch (Exception ignored) {
            return LookupResult.error("Stats service returned an invalid response. Please try again.");
        }

        PlayerProfile profile = parseProfile(player, guild);
        Bedwars bedwars = parseBedwars(player);
        Duels duels = parseDuels(player);
        return LookupResult.success(profile, bedwars, duels);
    }

    private static String statsEndpoint(UUID uuid) {
        String baseUrl = ModConfig.backendUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        try {
            URL url = new URL(baseUrl);
            String protocol = url.getProtocol().toLowerCase(Locale.ROOT);
            if (url.getHost().isEmpty()
                    || (!url.getPath().isEmpty() && !"/".equals(url.getPath()))
                    || url.getQuery() != null
                    || url.getRef() != null
                    || (!"https".equals(protocol) && !("http".equals(protocol) && isLoopbackHost(url.getHost())))) {
                throw new IllegalArgumentException(backendUrlError());
            }
        } catch (IOException ignored) {
            throw new IllegalArgumentException(backendUrlError());
        }
        return baseUrl + STATS_ENDPOINT + uuid.toString().replace("-", "");
    }

    private static boolean isLoopbackHost(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host)
                || "[::1]".equals(host);
    }

    private static String backendUrlError() {
        return "Stats backend URL must use HTTPS. HTTP is allowed only for local development.";
    }

    private static ApiResponse request(String endpoint) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(READ_TIMEOUT_MS);
                connection.setRequestProperty("Accept", "application/json");

                int status = connection.getResponseCode();
                String body = read(status >= 400 ? connection.getErrorStream() : connection.getInputStream());
                if (status == HttpURLConnection.HTTP_OK) {
                    return ApiResponse.success(body);
                }
                if (status == 429 || status >= 500) {
                    if (attempt < MAX_ATTEMPTS) {
                        sleep(attempt);
                        continue;
                    }
                    return ApiResponse.error(status == 429
                            ? "Stats service rate limit reached. Please try again shortly."
                            : "Stats service is temporarily unavailable. Please try again.");
                }
                return ApiResponse.error(errorForStatus(status, body));
            } catch (IOException ignored) {
                if (attempt < MAX_ATTEMPTS) {
                    sleep(attempt);
                    continue;
                }
                return ApiResponse.error("Could not connect to the stats service. Please try again.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return ApiResponse.error("Could not connect to the stats service. Please try again.");
    }

    private static void sleep(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static String errorForStatus(int status, String body) {
        try {
            return apiCause(new JsonParser().parse(body).getAsJsonObject(), "Stats service request failed (HTTP " + status + ").");
        } catch (Exception ignored) {
            return "Stats service request failed (HTTP " + status + ").";
        }
    }

    private static String read(InputStream input) throws IOException {
        if (input == null) {
            return "";
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static PlayerProfile parseProfile(JsonObject player, JsonObject guild) {
        String displayName = string(player, "displayname");
        return new PlayerProfile(
                displayName,
                formatRank(displayName, string(player, "newPackageRank"), string(player, "rankPlusColor"),
                        string(player, "monthlyPackageRank"), string(player, "monthlyRankColor"),
                        string(player, "rank"), string(player, "prefix")),
                formatGuildTag(string(guild, "tag"), string(guild, "tagColor"))
        );
    }

    private static Bedwars parseBedwars(JsonObject player) {
        JsonObject stats = object(player, "stats");
        JsonObject achievements = object(player, "achievements");
        JsonObject bedwars = object(stats, "Bedwars");
        if (bedwars == null || achievements == null) {
            return new Bedwars(-1, -1, -1, -1, -1, -1, -1, -1, -1D, -1D, -1D);
        }
        int finalKills = integer(bedwars, "final_kills_bedwars", 0);
        int finalDeaths = integer(bedwars, "final_deaths_bedwars", 0);
        int wins = integer(bedwars, "wins_bedwars", 0);
        int losses = integer(bedwars, "losses_bedwars", 0);
        int bedsBroken = integer(bedwars, "beds_broken_bedwars", 0);
        int bedsLost = integer(bedwars, "beds_lost_bedwars", 0);
        return new Bedwars(
                integer(achievements, "bedwars_level", 0), finalKills, bedsBroken, wins, losses, finalDeaths, bedsLost,
                integer(bedwars, "winstreak", -1), ratio(finalKills, finalDeaths), ratio(wins, losses), ratio(bedsBroken, bedsLost)
        );
    }

    private static Duels parseDuels(JsonObject player) {
        JsonObject stats = object(player, "stats");
        JsonObject duels = object(stats, "Duels");
        if (duels == null) {
            return new Duels(-1, -1, -1, -1, -1, -1, -1D, -1D, null);
        }
        int wins = integer(duels, "wins", 0);
        int losses = integer(duels, "losses", 0);
        int kills = integer(duels, "kills", 0);
        int deaths = integer(duels, "deaths", 0);
        double networkExp = decimal(player, "networkExp", 0D);
        String level = DuelsStatsCommand.levelColor(String.valueOf(round(getExactLevel(networkExp))));
        return new Duels(kills, deaths, wins, losses, integer(duels, "current_winstreak", -1),
                integer(duels, "best_overall_winstreak", -1), ratio(wins, losses), ratio(kills, deaths), level);
    }

    private static boolean hasFalseSuccess(JsonObject root) {
        return root.has("success") && !root.get("success").getAsBoolean();
    }

    private static String apiCause(JsonObject root, String fallback) {
        String cause = string(root, "cause");
        return cause == null || cause.trim().isEmpty() ? fallback : cause;
    }

    private static JsonObject object(JsonObject object, String property) {
        if (object == null || !object.has(property) || object.get(property).isJsonNull() || !object.get(property).isJsonObject()) {
            return null;
        }
        return object.getAsJsonObject(property);
    }

    private static String string(JsonObject object, String property) {
        if (object == null || !object.has(property) || object.get(property).isJsonNull()) {
            return null;
        }
        try {
            return object.get(property).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int integer(JsonObject object, String property, int fallback) {
        if (object == null || !object.has(property) || object.get(property).isJsonNull()) {
            return fallback;
        }
        try {
            return object.get(property).getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double decimal(JsonObject object, String property, double fallback) {
        if (object == null || !object.has(property) || object.get(property).isJsonNull()) {
            return fallback;
        }
        try {
            return object.get(property).getAsDouble();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double ratio(int numerator, int denominator) {
        return round(denominator == 0 ? numerator : (double) numerator / denominator);
    }

    private static double round(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private static final double BASE = 10000D;
    private static final double GROWTH = 2500D;

    private static double getExactLevel(double experience) {
        double level = getLevel(experience);
        double currentLevelExperience = totalExperienceToLevel(level);
        double nextLevelExperience = totalExperienceToLevel(level + 1D);
        return level + (experience - currentLevelExperience) / (nextLevelExperience - currentLevelExperience);
    }

    private static double getLevel(double experience) {
        double reversePrefix = -(BASE - 0.5D * GROWTH) / GROWTH;
        double reverseConstant = reversePrefix * reversePrefix;
        return experience < 0D ? 1D : Math.floor(1D + reversePrefix + Math.sqrt(reverseConstant + 2D * experience / GROWTH));
    }

    private static double totalExperienceToLevel(double level) {
        double wholeLevel = Math.floor(level);
        double current = totalExperienceToFullLevel(wholeLevel);
        if (level == wholeLevel) {
            return current;
        }
        return (totalExperienceToFullLevel(wholeLevel + 1D) - current) * (level % 1D) + current;
    }

    private static double totalExperienceToFullLevel(double level) {
        return (0.5D * GROWTH * (level - 2D) + BASE) * (level - 1D);
    }

    public static String formatRank(String displayName, String packageRank, String plusColor, String monthlyPackageRank,
                                    String monthlyRankColor, String specialRank, String prefix) {
        if (displayName == null) return null;
        if (displayName.equals("Technoblade")) return "§d[PIG§b+++§d]";
        if (displayName.equals("TommyInnit")) return "§d[INNIT]";
        if ("§6[MOJANG]".equals(prefix)) return "§6[MOJANG]";
        if ("STAFF".equals(specialRank)) return "§c[§6ዞ§c]";
        if ("YOUTUBER".equals(specialRank)) return "§c[§fYOUTUBE§c]";
        if ("MVP_PLUS".equals(packageRank)) {
            String color = plusColor(plusColor);
            if ("SUPERSTAR".equals(monthlyPackageRank)) {
                return "AQUA".equals(monthlyRankColor) ? "§b[MVP" + color + "++§b]" : "§6[MVP" + color + "++§6]";
            }
            return "§b[MVP" + color + "+§b]";
        }
        if ("MVP".equals(packageRank)) return "§b[MVP]";
        if ("VIP_PLUS".equals(packageRank)) return "§a[VIP§6+§a]";
        if ("VIP".equals(packageRank)) return "§a[VIP]";
        return "§7";
    }

    private static String plusColor(String color) {
        if ("GOLD".equals(color)) return "§6";
        if ("GREEN".equals(color)) return "§a";
        if ("YELLOW".equals(color)) return "§e";
        if ("LIGHT_PURPLE".equals(color)) return "§d";
        if ("WHITE".equals(color)) return "§f";
        if ("BLUE".equals(color)) return "§9";
        if ("DARK_GREEN".equals(color)) return "§2";
        if ("DARK_RED".equals(color)) return "§4";
        if ("DARK_AQUA".equals(color)) return "§3";
        if ("DARK_PURPLE".equals(color)) return "§5";
        if ("GRAY".equals(color)) return "§7";
        if ("DARK_GRAY".equals(color)) return "§8";
        if ("BLACK".equals(color)) return "§0";
        if ("DARK_BLUE".equals(color)) return "§1";
        return "§c";
    }

    public static String formatGuildTag(String tag, String tagColor) {
        if (tag == null || tag.trim().isEmpty()) return "";
        String sanitized = tag.replaceAll("[^a-zA-Z0-9✧θΘ✌✿✪➊✖❤✓]", "");
        if (sanitized.isEmpty()) return "";
        return chatColor(tagColor) + "[" + sanitized + "]";
    }

    private static String chatColor(String color) {
        if ("BLACK".equals(color)) return "§0";
        if ("DARK_BLUE".equals(color)) return "§1";
        if ("DARK_GREEN".equals(color)) return "§2";
        if ("DARK_AQUA".equals(color)) return "§3";
        if ("DARK_RED".equals(color)) return "§4";
        if ("DARK_PURPLE".equals(color)) return "§5";
        if ("GOLD".equals(color)) return "§6";
        if ("GRAY".equals(color)) return "§7";
        if ("DARK_GRAY".equals(color)) return "§8";
        if ("BLUE".equals(color)) return "§9";
        if ("GREEN".equals(color)) return "§a";
        if ("AQUA".equals(color)) return "§b";
        if ("RED".equals(color)) return "§c";
        if ("LIGHT_PURPLE".equals(color)) return "§d";
        if ("YELLOW".equals(color)) return "§e";
        if ("WHITE".equals(color)) return "§f";
        return "§7";
    }

    public static final class LookupResult {
        private final PlayerProfile profile;
        private final Bedwars bedwars;
        private final Duels duels;
        private final String error;

        private LookupResult(PlayerProfile profile, Bedwars bedwars, Duels duels, String error) {
            this.profile = profile;
            this.bedwars = bedwars;
            this.duels = duels;
            this.error = error;
        }

        public static LookupResult success(PlayerProfile profile, Bedwars bedwars, Duels duels) {
            return new LookupResult(profile, bedwars, duels, null);
        }

        public static LookupResult error(String error) {
            return new LookupResult(null, null, null, error);
        }

        public boolean isSuccess() { return error == null; }
        public PlayerProfile getProfile() { return profile; }
        public Bedwars getBedwars() { return bedwars; }
        public Duels getDuels() { return duels; }
        public String getError() { return error; }
    }

    private static final class ApiResponse {
        private final String body;
        private final String error;

        private ApiResponse(String body, String error) {
            this.body = body;
            this.error = error;
        }

        static ApiResponse success(String body) { return new ApiResponse(body, null); }
        static ApiResponse error(String error) { return new ApiResponse(null, error); }
        boolean isSuccess() { return error == null; }
    }
}
