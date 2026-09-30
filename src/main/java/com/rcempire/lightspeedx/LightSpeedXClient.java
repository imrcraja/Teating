package com.rcempire.lightspeedx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class LightSpeedXClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        HudRenderCallback.EVENT.register(LightSpeedXClient::renderHud);
    }
    private static void renderHud(GuiGraphics graphics, DeltaTracker tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && LightSpeedX.isActive(mc.player)) {
            graphics.drawString(mc.font, "LightSpeedX  •  ROCKET MODE", 8, 8, 0xFFFFFF, true);
            graphics.drawString(mc.font, "Look = direction | Sprint = boost | Sneak = down", 8, 20, 0xB8C7D9, true);
        }
    }
}
