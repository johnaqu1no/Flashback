package com.moulberry.flashback;

import com.moulberry.flashback.state.EditorState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Detects servers which replace the vanilla nametag with a text display entity.
 * <p>
 * Three strategies, in order of confidence:
 * <ol>
 *     <li>a text display riding the player whose sanitized text contains the profile name</li>
 *     <li>any text display riding the player (nickname plugins hide the profile name)</li>
 *     <li>an unmounted text display floating around the player's head whose text contains the
 *     profile name (servers which teleport the display instead of mounting it)</li>
 * </ol>
 */
public class TextDisplayNametags {

    private static final double NEARBY_HORIZONTAL_RADIUS = 1.0;
    private static final double NEARBY_HEIGHT_ABOVE_HEAD = 2.0;

    public static List<Display.TextDisplay> findFor(Player player) {
        String name = player.getScoreboardName().toLowerCase(Locale.ROOT);

        List<Display.TextDisplay> named = new ArrayList<>();
        List<Display.TextDisplay> mounted = new ArrayList<>();
        for (Entity passenger : player.getIndirectPassengers()) {
            if (passenger instanceof Display.TextDisplay textDisplay) {
                mounted.add(textDisplay);
                if (containsName(textDisplay, name)) {
                    named.add(textDisplay);
                }
            }
        }

        if (!named.isEmpty()) {
            return named;
        }
        if (!mounted.isEmpty()) {
            return mounted;
        }
        return findNearby(player, name);
    }

    public static List<UUID> findUuidsFor(Player player) {
        List<UUID> uuids = new ArrayList<>();
        for (Display.TextDisplay textDisplay : findFor(player)) {
            uuids.add(textDisplay.getUUID());
        }
        return uuids;
    }

    /**
     * Text displays which aren't mounted but hover around the player's head. Requires a name
     * match, otherwise unrelated holograms would get picked up.
     */
    private static List<Display.TextDisplay> findNearby(Player player, String lowercaseName) {
        AABB box = new AABB(
            player.getX() - NEARBY_HORIZONTAL_RADIUS, player.getY(), player.getZ() - NEARBY_HORIZONTAL_RADIUS,
            player.getX() + NEARBY_HORIZONTAL_RADIUS, player.getY() + player.getBbHeight() + NEARBY_HEIGHT_ABOVE_HEAD,
            player.getZ() + NEARBY_HORIZONTAL_RADIUS
        );
        return player.level().getEntitiesOfClass(Display.TextDisplay.class, box,
            textDisplay -> containsName(textDisplay, lowercaseName));
    }

    /**
     * Resolves the name override of the player a mounted nametag display belongs to, so the
     * override keeps applying when the server respawns the display with a new uuid.
     */
    public static String resolveNameOverride(Display.TextDisplay display, EditorState editorState) {
        if (editorState.nameOverride.isEmpty()) {
            return null;
        }
        Entity vehicle = display.getVehicle();
        while (vehicle != null) {
            if (vehicle instanceof Player player) {
                String override = editorState.nameOverride.get(player.getUUID());
                if (override != null && containsName(display, player.getScoreboardName().toLowerCase(Locale.ROOT))) {
                    return override;
                }
                return null;
            }
            vehicle = vehicle.getVehicle();
        }
        return null;
    }

    private static boolean containsName(Display.TextDisplay display, String lowercaseName) {
        Component text = display.getText();
        if (text == null) {
            return false;
        }
        String sanitized = sanitize(text);
        return !sanitized.isEmpty() && sanitized.toLowerCase(Locale.ROOT).contains(lowercaseName);
    }

    /**
     * Flattens a component to plain text and strips any embedded legacy formatting codes.
     */
    public static String sanitize(Component text) {
        String plain = ChatFormatting.stripFormatting(text.getString());
        return plain == null ? "" : plain.trim();
    }

}
