package org.hyperoil.playifkillers.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.Bukkit;
import org.hyperoil.playifkillers.disguiseMe;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class APIUtils {
    // TODO: figure out how to use the official mojang api(s) instead of the playerdb.co (problem i need the skin signature)
    private static final String UUID_API = "https://playerdb.co/api/player/minecraft/%s";
    private static HttpClient httpClient = HttpClient.newBuilder().
            followRedirects(HttpClient.Redirect.NEVER).
            build();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Cache<UUID, APIResponse> responseCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1))
            .build();
    private static Cache<String, UUID> UUIDCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1))
            .build();
    public static APIResponse fetchPlayer(UUID uuid) {
        APIResponse cached = responseCache.getIfPresent(uuid);
        if (cached != null) {
            return cached;
        }
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(String.format(UUID_API, uuid.toString()))).
                GET().
                build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode data = rootNode.get("data");
            JsonNode player = data.get("player");
            String username = player.get("username").textValue();
            JsonNode properties = player.get("properties");
            JsonNode textures = properties.get(0);
            String skinTexture = textures.get("value").textValue();
            String signature = textures.get("signature").textValue();
            Skin skin = new Skin(uuid, skinTexture, signature);
            APIResponse response = new APIResponse(uuid, username, skin);
            responseCache.put(uuid, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static APIResponse fetchPlayer(String username) {
        UUID cached = UUIDCache.getIfPresent(username);
        if (cached != null) {
            return fetchPlayer(cached);
        }
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(String.format(UUID_API, username))).
                GET().
                build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(httpResponse.body());
            JsonNode data = rootNode.get("data");
            JsonNode player = data.get("player");
            UUID uuid = UUID.fromString(player.get("id").textValue());
            JsonNode properties = player.get("properties");
            JsonNode textures = properties.get(0);
            String skinTexture = textures.get("value").textValue();
            String signature = textures.get("signature").textValue();
            Skin skin = new Skin(uuid, skinTexture, signature);
            APIResponse response = new APIResponse(uuid, username, skin);
            responseCache.put(uuid, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
