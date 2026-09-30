package com.rcempire.lightspeedx;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LightSpeedX {
    public static final String MOD_ID = "lightspeedx";
    private static final ResourceLocation ROCKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "rocket_core");
    private static final ResourceKey<Item> ROCKET_KEY = ResourceKey.create(Registries.ITEM, ROCKET_ID);
    public static final Item ROCKET_CORE = new Item(new Item.Properties().setId(ROCKET_KEY).stacksTo(1));
    private static final Set<UUID> ACTIVE = new HashSet<>();

    public static void init() {
        BuiltInRegistries.ITEM.register(ROCKET_ID, ROCKET_CORE);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> e.accept(ROCKET_CORE));
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide || !player.getItemInHand(hand).is(ROCKET_CORE)) return InteractionResult.PASS;
            UUID id = player.getUUID();
            if (ACTIVE.remove(id))
                player.displayClientMessage(Component.literal("LightSpeedX: rocket disengaged"), true);
            else {
                ACTIVE.add(id);
                player.displayClientMessage(Component.literal("LightSpeedX: rocket engaged"), true);
            }
            return InteractionResult.SUCCESS;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers())
                if (ACTIVE.contains(player.getUUID())) tickRocket(player);
        });
    }

    private static void tickRocket(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        double speed = player.isSprinting() ? 42.0 : 6.0;
        Vec3 movement = look.scale(speed);
        if (player.isShiftKeyDown()) movement = new Vec3(movement.x, -speed, movement.z);

        AABB impactBox = player.getBoundingBox()
            .expandTowards(movement.normalize().scale(Math.min(speed, 8.0))).inflate(0.4);
        for (Entity other : player.level().getEntities(player, impactBox,
                e -> e != player && !player.isPassengerOfSameVehicle(e))) {
            if (!other.isSpectator()) {
                Vec3 impulse = look.scale(Math.min(1.8, speed * 0.035));
                other.push(impulse.x, Math.max(0.08, impulse.y), impulse.z);
                movement = movement.scale(0.25);
                break;
            }
        }

        player.setDeltaMovement(movement);
        player.hurtMarked = true;
        if (speed > 6.0 && player.tickCount % 2 == 0) {
            player.serverLevel().sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()),
                player.getX() - look.x * 1.2, player.getY() - look.y * 1.2, player.getZ() - look.z * 1.2,
                2, 0.12, 0.12, 0.12, 0.03);
        }
    }

    public static boolean isActive(Player player) { return ACTIVE.contains(player.getUUID()); }
}
