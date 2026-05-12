package com.moulberry.flashback.keyframe.change;

import com.moulberry.flashback.keyframe.handler.KeyframeHandler;

import java.util.Set;
import java.util.UUID;

public record KeyframeChangeFreezeGameTime(boolean frozen, boolean allowAllPlayers, Set<UUID> exemptEntities) implements KeyframeChange {
    @Override
    public void apply(KeyframeHandler keyframeHandler) {
        keyframeHandler.applyFreezeGameTime(this.frozen, this.allowAllPlayers, this.exemptEntities);
    }

    @Override
    public KeyframeChange interpolate(KeyframeChange to, double amount) {
        return this;
    }
}
