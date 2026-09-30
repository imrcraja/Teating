package com.rcempire.lightspeedx;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.commands.Commands;
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

import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class LightSpeedX implements ModInitializer {
    public static final String MOD_ID = "lightspeedx";
    private static final ResourceLocation ROCKET_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "rocket_core");
    private static final ResourceKey<Item> ROCKET_KEY =
            ResourceKey.create(Registries.ITEM, ROCKET_ID);
    public static final Item ROCKET_CORE =
            new Item(new Item.Properties().setId(ROCKET_KEY).stacksTo(1));

    private static final Map<UUID, Double> SPEEDS = new HashMap<>();
    private static final java.util.Set<UUID> ACTIVE = new java.util.HashSet<>();
    private static final double DEFAULT_SPEED = 6.0;
    private static final double MAX_SPEED = 50_000_000.0;
    private static final Map<UUID, Long> LAUNCH_TICKS = new HashMap<>();

    @Override
    public void onInitialize() {
        BuiltInRegistries.ITEM.register(ROCKET_ID, ROCKET_CORE);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(ROCKET_CORE));

        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide || !player.getItemInHand(hand).is(ROCKET_CORE))
                return InteractionResult.PASS;

            UUID id = player.getUUID();
            if (ACTIVE.remove(id)) {
                player.displayClientMessage(Component.literal("LightSpeedX: rocket disengaged"), true);
            } else {
                ACTIVE.add(id);
                SPEEDS.putIfAbsent(id, DEFAULT_SPEED);
                LAUNCH_TICKS.put(id, player.tickCount);
                player.displayClientMessage(
                        Component.literal("LightSpeedX: rocket engaged | speed "
                                + formatSpeed(SPEEDS.get(id))), true);
            }
            return InteractionResult.SUCCESS;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
                dispatcher.register(literal("lightspeedx")
                        .then(literal("speed")
                                .then(argument("blocksPerTick",
                                                DoubleArgumentType.doubleArg(0.01, MAX_SPEED))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            double speed = getDouble(context, "blocksPerTick");
                                            SPEEDS.put(player.getUUID(), speed);
                                            player.displayClientMessage(
                                                    Component.literal("LightSpeedX speed set to "
                                                            + formatSpeed(speed)
                                                            + " blocks/tick"), false);
                                            return 1;
                                        })))
                        .then(literal("on").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            SPEEDS.putIfAbsent(player.getUUID(), DEFAULT_SPEED);
                            ACTIVE.add(player.getUUID());
                            return 1;
                        }))
                        .then(literal("off").executes(context -> {
                            UUID id = context.getSource().getPlayerOrException().getUUID();
                            ACTIVE.remove(id);
                            LAUNCH_TICKS.remove(id);
                            return 1;
                        }))
                        .then(literal("info").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            double speed = SPEEDS.getOrDefault(player.getUUID(), DEFAULT_SPEED);
                            player.displayClientMessage(Component.literal(
                                    "LightSpeedX | " + formatSpeed(speed) + " blocks/tick"), false);
                            return 1;
                        }))));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (ACTIVE.contains(player.getUUID()))
                    tickRocket(player);
            }
        });
    }

    private static void tickRocket(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        double speed = SPEEDS.getOrDefault(player.getUUID(), DEFAULT_SPEED);
        long launchAge = player.tickCount - LAUNCH_TICKS.getOrDefault(player.getUUID(), (long) player.tickCount);
        double ramp = Math.min(1.0, launchAge / 10.0);
        speed *= ramp;
        Vec3 movement = look.scale(speed);

        if (player.isShiftKeyDown())
            movement = new Vec3(movement.x, -speed, movement.z);

        // Keep entity collision cheap: query only the swept corridor around the rocket.
        double queryDistance = Math.min(speed, 16.0);
        AABB impactBox = player.getBoundingBox()
                .expandTowards(look.scale(queryDistance))
                .inflate(0.45);

        for (Entity other : player.level().getEntities(player, impactBox,
                entity -> entity != player && !player.isPassengerOfSameVehicle(entity))) {
            if (!other.isSpectator()) {
                Vec3 impulse = look.scale(Math.min(3.0, speed * 0.035));
                other.push(impulse.x, Math.max(0.08, impulse.y), impulse.z);
                movement = movement.scale(0.15);
                break;
            }
        }

        player.setDeltaMovement(movement);
        player.hurtMarked = true;

        // Deliberately tiny particle budget for low-end devices.
        if (speed > DEFAULT_SPEED && player.tickCount % 3 == 0) {
            player.serverLevel().sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()),
                    player.getX() - look.x * 1.2,
                    player.getY() - look.y * 1.2,
                    player.getZ() - look.z * 1.2,
                    1, 0.08, 0.08, 0.08, 0.01);
        }
    }

    private static String formatSpeed(double value) {
        if (value >= 1_000_000) return String.format(java.util.Locale.ROOT, "%.2fM", value);
        if (value >= 1_000) return String.format(java.util.Locale.ROOT, "%.2fk", value);
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    public static boolean isActive(Player player) {
        return ACTIVE.contains(player.getUUID());
    }

    public static double getSpeed(Player player) {
        return SPEEDS.getOrDefault(player.getUUID(), DEFAULT_SPEED);
    }
}
