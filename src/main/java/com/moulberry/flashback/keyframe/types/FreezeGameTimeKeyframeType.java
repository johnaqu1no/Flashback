package com.moulberry.flashback.keyframe.types;

import com.moulberry.flashback.editor.ui.ImGuiHelper;
import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.change.KeyframeChangeFreezeGameTime;
import com.moulberry.flashback.keyframe.impl.FreezeGameTimeKeyframe;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import imgui.moulberry90.ImGui;
import imgui.moulberry90.type.ImBoolean;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class FreezeGameTimeKeyframeType implements KeyframeType<FreezeGameTimeKeyframe> {

    public static FreezeGameTimeKeyframeType INSTANCE = new FreezeGameTimeKeyframeType();

    private FreezeGameTimeKeyframeType() {
    }

    @Override
    public Class<? extends KeyframeChange> keyframeChangeType() {
        return KeyframeChangeFreezeGameTime.class;
    }

    @Override
    public @Nullable String icon() {
        return "";
    }

    @Override
    public String name() {
        return I18n.get("flashback.keyframe.freeze_game_time");
    }

    @Override
    public String id() {
        return "FREEZE_GAME_TIME";
    }

    @Override
    public boolean allowChangingInterpolationType() {
        return false;
    }

    @Override
    public boolean hasCustomKeyframeChangeCalculation() {
        return true;
    }

    @Override
    public KeyframeChange customKeyframeChange(TreeMap<Integer, Keyframe> keyframes, float tick) {
        if (keyframes.isEmpty()) {
            return null;
        }

        int currentTick = (int) tick;
        NavigableMap<Integer, Keyframe> head = keyframes.headMap(currentTick, true);
        int countBeforeOrAt = head.size();

        // Pair semantics: 1st keyframe = freeze start, 2nd = freeze end, 3rd = freeze start, ...
        // Frozen only when inside a complete pair (a closing keyframe must exist after).
        boolean frozen = (countBeforeOrAt % 2) == 1 && keyframes.size() > countBeforeOrAt;

        boolean allowAllPlayers = true;
        if (!head.isEmpty() && head.lastEntry().getValue() instanceof FreezeGameTimeKeyframe startKf) {
            allowAllPlayers = startKf.allowAllPlayers();
        }

        Set<UUID> exempt = Set.of();
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null) {
            exempt = Set.copyOf(editorState.freezeExemptEntities);
        }

        return new KeyframeChangeFreezeGameTime(frozen, allowAllPlayers, exempt);
    }

    @Override
    public @Nullable FreezeGameTimeKeyframe createDirect() {
        return null;
    }

    @Override
    public KeyframeCreatePopup<FreezeGameTimeKeyframe> createPopup() {
        ImBoolean allowAllPlayers = new ImBoolean(true);

        return () -> {
            ImGui.checkbox(I18n.get("flashback.allow_all_players"), allowAllPlayers);
            ImGuiHelper.tooltip(I18n.get("flashback.allow_all_players_tooltip"));

            if (ImGui.button(I18n.get("flashback.add"))) {
                return new FreezeGameTimeKeyframe(allowAllPlayers.get());
            }
            ImGui.sameLine();
            if (ImGui.button(I18n.get("gui.cancel"))) {
                ImGui.closeCurrentPopup();
            }
            return null;
        };
    }
}
