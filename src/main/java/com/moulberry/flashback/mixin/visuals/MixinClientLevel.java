package com.moulberry.flashback.mixin.visuals;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    /*
     * Some entities have weird behaviour when rotating in a replay
     * This code here ensures that the interpolation of yRotO -> yRot will always be the shortest path
     */

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true, require = 0)
    public void flashback$tickNonPassengerGameTimeFreeze(Entity entity, CallbackInfo ci) {
        ReplayServer replayServer = Flashback.getReplayServer();
        if (replayServer != null && replayServer.isGameTimeFrozen() && !replayServer.isGameTimeExempt(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true, require = 0)
    public void flashback$tickPassengerGameTimeFreeze(Entity vehicle, Entity passenger, CallbackInfo ci) {
        ReplayServer replayServer = Flashback.getReplayServer();
        if (replayServer != null && replayServer.isGameTimeFrozen() && !replayServer.isGameTimeExempt(passenger)) {
            ci.cancel();
        }
    }

    @Inject(method = "tickNonPassenger", at = @At("RETURN"), require = 0)
    public void tickNonPassengerEnd(Entity entity, CallbackInfo ci) {
        if (Flashback.isInReplay()) {
            entity.xRotO = Mth.wrapDegrees(entity.xRotO - entity.getXRot()) + entity.getXRot();
            entity.yRotO = Mth.wrapDegrees(entity.yRotO - entity.getYRot()) + entity.getYRot();
        }
    }

    @Inject(method = "tickPassenger", at = @At("RETURN"), require = 0)
    public void tickPassengerEnd(Entity vehicle, Entity entity, CallbackInfo ci) {
        if (Flashback.isInReplay()) {
            entity.xRotO = Mth.wrapDegrees(entity.xRotO - entity.getXRot()) + entity.getXRot();
            entity.yRotO = Mth.wrapDegrees(entity.yRotO - entity.getYRot()) + entity.getYRot();
        }
    }

}
