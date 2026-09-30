package com.rcempire.lightspeedx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class LightSpeedXClient implements ClientModInitializer {
    private static final Identifier HUD_ID =
            Identifier.fromNamespaceAndPath(LightSpeedX.MOD_ID, "rocket_hud");
    private static ItemEntity rocketVisual;
    private static long animationTicks;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(HUD_ID, LightSpeedXClient::renderHud);
    }

    private static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            removeVisual();
            return;
        }

        if (LightSpeedX.isActive(mc.player)) {
            ensureVisual(mc);
            updateVisual(mc, tickDelta.getGameTimeDeltaPartialTick(false));

            graphics.text(mc.font, "LightSpeedX  •  ROCKET MODE", 8, 8, 0xFFFFFFFF, true);
            graphics.text(mc.font,
                    "Look = direction | Sneak = down | Speed = " + formatSpeed(LightSpeedX.getSpeed(mc.player)),
                    8, 20, 0xFFB8C7D9, true);
        } else {
            removeVisual();
        }
    }

    private static void ensureVisual(Minecraft mc) {
        if (rocketVisual != null && !rocketVisual.isRemoved() && rocketVisual.level() == mc.level)
            return;

        if (mc.level == null) return;

        rocketVisual = new ItemEntity(mc.level, mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                new ItemStack(LightSpeedX.ROCKET_CORE));
        rocketVisual.setNoGravity(true);
        rocketVisual.setPickUpDelay(32767);
        rocketVisual.setDeltaMovement(Vec3.ZERO);
        mc.level.addFreshEntity(rocketVisual);
    }

    private static void updateVisual(Minecraft mc, float partialTick) {
        if (rocketVisual == null || mc.player == null) return;

        animationTicks++;
        Vec3 look = mc.player.getLookAngle().normalize();
        double pulse = Math.sin((animationTicks + partialTick) * 0.55) * 0.035;
        double bob = Math.sin((animationTicks + partialTick) * 0.32) * 0.025;

        Vec3 center = mc.player.position()
                .add(look.scale(0.35 + pulse))
                .add(0, 0.15 + bob, 0);

        rocketVisual.setPos(center.x, center.y, center.z);
        rocketVisual.setDeltaMovement(Vec3.ZERO);

        float yaw = (float) (Math.atan2(look.z, look.x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-Math.asin(look.y) * 180.0 / Math.PI);
        float bank = (float) Math.sin((animationTicks + partialTick) * 0.22) * 3.0f;
        rocketVisual.setYRot(yaw + bank);
        rocketVisual.setXRot(pitch);
    }

    private static void removeVisual() {
        if (rocketVisual != null) {
            rocketVisual.discard();
            rocketVisual = null;
        }
    }

    private static String formatSpeed(double value) {
        if (value >= 1_000_000) return String.format(java.util.Locale.ROOT, "%.2fM", value);
        if (value >= 1_000) return String.format(java.util.Locale.ROOT, "%.2fk", value);
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
