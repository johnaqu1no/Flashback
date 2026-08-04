package com.moulberry.flashback.mixin.visuals;

import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The render state of a text display is only rebuilt on tick, so replacing the entity's text
 * during rendering has no effect. These accessors let us swap the already-built state instead.
 */
@Mixin(Display.TextDisplay.class)
public interface MixinTextDisplayAccessor {

    @Accessor("textRenderState")
    Display.TextDisplay.TextRenderState flashback$getTextRenderState();

    @Accessor("textRenderState")
    void flashback$setTextRenderState(Display.TextDisplay.TextRenderState textRenderState);

    @Accessor("clientDisplayCache")
    void flashback$setClientDisplayCache(Display.TextDisplay.CachedInfo cachedInfo);

}
