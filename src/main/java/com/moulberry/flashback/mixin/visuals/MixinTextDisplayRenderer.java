package com.moulberry.flashback.mixin.visuals;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public class MixinTextDisplayRenderer {

    @Unique
    private Component flashback$originalText;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("HEAD"))
    public void extractRenderStateHead(Display.TextDisplay entity, TextDisplayEntityRenderState state, float partialTick, CallbackInfo ci) {
        flashback$originalText = null;
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState == null) return;

        String textOverrideJson = editorState.textDisplayTextOverride.get(entity.getUUID());
        if (textOverrideJson != null) {
            try {
                Component newText = Component.Serializer.fromJsonLenient(textOverrideJson, entity.registryAccess());
                if (newText != null) {
                    flashback$originalText = entity.getText();
                    entity.setText(newText);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("RETURN"))
    public void extractRenderStateReturn(Display.TextDisplay entity, TextDisplayEntityRenderState state, float partialTick, CallbackInfo ci) {
        // Restore original text after extraction
        if (flashback$originalText != null) {
            entity.setText(flashback$originalText);
            flashback$originalText = null;
        }

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
