package dev.bergthaler.cubebuster.event;

import dev.bergthaler.cubebuster.Config;
import dev.bergthaler.cubebuster.Cubebuster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Raises a player's aggro score (see {@link AggroManager}) whenever they open a container (chest, furnace,
 * crafting table, any modded container - {@link PlayerContainerEvent.Open} fires for all of them) or use a bed
 * (matched via the vanilla {@link BlockTags#BEDS} tag, so modded beds using it are covered too). Also resets
 * the score to 0 on death.
 * <p>
 * Replaces the old SiegeZombieInteractionSpawnHandler, which spawned SiegeZombies directly on interaction; now
 * interactions only feed the score, and {@link AggroSpawnHandler}'s periodic check decides what (if anything)
 * spawns from it.
 */
@EventBusSubscriber(modid = Cubebuster.MODID)
public final class AggroInteractionHandler {

    // Per-player cooldown so rapidly opening/closing a container can't spam score gain.
    private static final Map<UUID, Long> lastGainGameTime = new HashMap<>();

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            maybeGainScore(player);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (state.is(BlockTags.BEDS)) {
            maybeGainScore(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AggroManager.reset(player);
        }
    }

    private static void maybeGainScore(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        Long last = lastGainGameTime.get(player.getUUID());
        if (last != null && now - last < Config.aggroInteractionCooldownTicks) {
            return;
        }
        lastGainGameTime.put(player.getUUID(), now);
        AggroManager.addScore(player, Config.aggroGainPerInteraction);
    }

    private AggroInteractionHandler() {
    }
}
