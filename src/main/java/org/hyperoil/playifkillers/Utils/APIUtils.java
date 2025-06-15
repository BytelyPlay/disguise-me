package org.hyperoil.playifkillers.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.hyperoil.playifkillers.disguiseMe;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.UUID;

public class APIUtils {
    // TODO: figure out how to use the official mojang api(s) instead of the playerdb.co (problem i need the skin signature)
    private static final String UUID_API = "https://playerdb.co/api/player/minecraft/%s";
    private static HttpClient httpClient = HttpClient.newBuilder().
            followRedirects(HttpClient.Redirect.NEVER).
            build();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static HashMap<UUID, APIResponse> responseCache = new HashMap<>();
    private static HashMap<String, UUID> UUIDCache = new HashMap<>();
    public static APIResponse fetchPlayer(UUID uuid) {
        if (responseCache.containsKey(uuid)) {
            return responseCache.get(uuid);
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
        if (UUIDCache.containsKey(username)) {
            return fetchPlayer(UUIDCache.get(username));
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
    public static void initCache() {
        Bukkit.getScheduler().runTaskTimer(disguiseMe.getInstance(), () -> {
            responseCache.clear();
        }, 12000, 12000);
    }
}
