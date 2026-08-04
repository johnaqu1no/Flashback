package com.moulberry.flashback;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parses name override strings (MiniMessage or legacy colour codes) into vanilla components,
 * caching the result since name lookups happen every frame.
 */
public class FormattedNameCache {

    private static final int MAX_CACHE_SIZE = 256;
    private static final Map<String, Component> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Component> JSON_CACHE = new ConcurrentHashMap<>();

    public static Component get(String input, RegistryAccess registryAccess) {
        Component cached = CACHE.get(input);
        if (cached != null) {
            return cached;
        }

        if (CACHE.size() >= MAX_CACHE_SIZE) {
            CACHE.clear();
        }

        Component parsed = null;
        try {
            JsonElement json = JsonParser.parseString(Flashback.parseFormattedText(input));
            RegistryOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
            parsed = ComponentSerialization.CODEC.parse(ops, json).result().orElse(null);
        } catch (Exception ignored) {
        }
        if (parsed == null) {
            parsed = Component.literal(input);
        }

        CACHE.put(input, parsed);
        return parsed;
    }

    /**
     * Parses a serialized component (as stored in the editor state), caching the result since
     * text display overrides are resolved every frame.
     */
    public static Component fromJson(String json, RegistryAccess registryAccess) {
        Component cached = JSON_CACHE.get(json);
        if (cached != null) {
            return cached;
        }

        if (JSON_CACHE.size() >= MAX_CACHE_SIZE) {
            JSON_CACHE.clear();
        }

        Component parsed;
        try {
            RegistryOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
            parsed = ComponentSerialization.CODEC.parse(ops, JsonParser.parseString(json)).result().orElse(null);
        } catch (Exception e) {
            parsed = null;
        }
        if (parsed == null) {
            return null;
        }

        JSON_CACHE.put(json, parsed);
        return parsed;
    }

    /**
     * Returns the plain text of a formatted name, e.g. "&lt;red&gt;Steve&lt;/red&gt;" -> "Steve".
     * Used when the input needs to act as a real username (skin lookups).
     */
    public static String plainText(String input, RegistryAccess registryAccess) {
        return get(input, registryAccess).getString();
    }

}
