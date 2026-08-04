package com.moulberry.flashback.mixin.visuals;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.FormattedNameCache;
import com.moulberry.flashback.TextDisplayNametags;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public class MixinTextDisplayRenderer {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("HEAD"))
    public void extractRenderStateHead(Display.TextDisplay entity, TextDisplayEntityRenderState state, float partialTick, CallbackInfo ci) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState == null) return;

        MixinTextDisplayAccessor accessor = (MixinTextDisplayAccessor) entity;
        Display.TextDisplay.TextRenderState current = accessor.flashback$getTextRenderState();
        if (current == null) return;

        // The desired text falls back to the entity's real text, so removing an override restores it
        Component desired = resolveOverride(entity, editorState);
        if (desired == null) {
            desired = entity.getText();
        }
        if (desired == null || desired.equals(current.text())) {
            return;
        }

        accessor.flashback$setTextRenderState(new Display.TextDisplay.TextRenderState(desired, current.lineWidth(),
            current.textOpacity(), current.backgroundColor(), current.flags()));
        accessor.flashback$setClientDisplayCache(null);
    }

    private static Component resolveOverride(Display.TextDisplay entity, EditorState editorState) {
        String overrideJson = editorState.textDisplayTextOverride.get(entity.getUUID());
        if (overrideJson != null) {
            return FormattedNameCache.fromJson(overrideJson, entity.registryAccess());
        }

        // Nametag displays get respawned with new uuids, so also follow the player being renamed
        String nameOverride = TextDisplayNametags.resolveNameOverride(entity, editorState);
        if (nameOverride != null) {
            return FormattedNameCache.get(nameOverride, entity.registryAccess());
        }
        return null;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("RETURN"))
    public void extractRenderStateReturn(Display.TextDisplay entity, TextDisplayEntityRenderState state, float partialTick, CallbackInfo ci) {
        // Apply 15% opacity for hidden text displays (replay preview only, fully hidden on export)
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState != null && !Flashback.isExporting() && editorState.hideDuringExport.contains(entity.getUUID()) && state.textRenderState != null) {
            Display.TextDisplay.TextRenderState original = state.textRenderState;

            Display.IntInterpolator opacityInterp = Display.IntInterpolator.constant(38);

            Display.IntInterpolator originalBg = original.backgroundColor();
            Display.IntInterpolator bgInterp = progress -> {
                int color = originalBg.get(progress);
                int alpha = (color >> 24) & 0xFF;
                int newAlpha = (int) (alpha * 0.15f);
                return (newAlpha << 24) | (color & 0x00FFFFFF);
            };

            state.textRenderState = new Display.TextDisplay.TextRenderState(
                original.text(),
                original.lineWidth(),
                opacityInterp,
                bgInterp,
                original.flags()
            );
        }
    }

}
