package com.moulberry.flashback.keyframe.impl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.KeyframeType;
import com.moulberry.flashback.keyframe.change.KeyframeChange;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.keyframe.types.FreezeGameTimeKeyframeType;
import imgui.moulberry90.ImGui;
import net.minecraft.client.resources.language.I18n;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;

public class FreezeGameTimeKeyframe extends Keyframe {

    private boolean allowAllPlayers = true;

    public FreezeGameTimeKeyframe() {
        this(true, InterpolationType.HOLD);
    }

    public FreezeGameTimeKeyframe(boolean allowAllPlayers) {
        this(allowAllPlayers, InterpolationType.HOLD);
    }

    public FreezeGameTimeKeyframe(boolean allowAllPlayers, InterpolationType interpolationType) {
        this.allowAllPlayers = allowAllPlayers;
        this.interpolationType(interpolationType);
    }

    public boolean allowAllPlayers() {
        return this.allowAllPlayers;
    }

    @Override
    public KeyframeType<?> keyframeType() {
        return FreezeGameTimeKeyframeType.INSTANCE;
    }

    @Override
    public Keyframe copy() {
        return new FreezeGameTimeKeyframe(this.allowAllPlayers, this.interpolationType());
    }

    @Override
    public void renderEditKeyframe(Consumer<Consumer<Keyframe>> update) {
        ImGui.setNextItemWidth(160);
        if (ImGui.checkbox(I18n.get("flashback.allow_all_players"), this.allowAllPlayers)) {
            boolean newValue = !this.allowAllPlayers;
            update.accept(keyframe -> ((FreezeGameTimeKeyframe) keyframe).allowAllPlayers = newValue);
        }
    }

    @Override
    public KeyframeChange createChange() {
        return null;
    }

    @Override
    public KeyframeChange createSmoothInterpolatedChange(Keyframe p1, Keyframe p2, Keyframe p3, float t0, float t1, float t2, float t3, float amount) {
        return null;
    }

    @Override
    public KeyframeChange createHermiteInterpolatedChange(Map<Float, Keyframe> keyframes, float amount) {
        return null;
    }

    public static class TypeAdapter implements JsonSerializer<FreezeGameTimeKeyframe>, JsonDeserializer<FreezeGameTimeKeyframe> {
        @Override
        public FreezeGameTimeKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            boolean allowAllPlayers = jsonObject.has("allow_all_players") ? jsonObject.get("allow_all_players").getAsBoolean() : true;
            InterpolationType interpolationType = context.deserialize(jsonObject.get("interpolation_type"), InterpolationType.class);
            return new FreezeGameTimeKeyframe(allowAllPlayers, interpolationType);
        }

        @Override
        public JsonElement serialize(FreezeGameTimeKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("allow_all_players", src.allowAllPlayers);
            jsonObject.addProperty("type", "freeze_game_time");
            jsonObject.add("interpolation_type", context.serialize(src.interpolationType()));
            return jsonObject;
        }
    }
}
