package org.hyperoil.playifkillers.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.hyperoil.playifkillers.disguiseMe;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.UUID;

public class SkinFetcher {
    private static final String UUID_API = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
    private static HttpClient httpClient = HttpClient.newBuilder().
            followRedirects(HttpClient.Redirect.NEVER).
            build();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static HashMap<UUID, String> cache = new HashMap<>();
    public static String getPlayerSkin(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(String.format(UUID_API, uuid.toString()))).
                GET().
                build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode properties = rootNode.get("properties");
            JsonNode textures = properties.get(0);
            return textures.get("value").textValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void initCache() {
        Bukkit.getScheduler().runTaskTimer(disguiseMe.getInstance(), () -> {
            cache.clear();
        }, 12000, 12000);
    }
}
