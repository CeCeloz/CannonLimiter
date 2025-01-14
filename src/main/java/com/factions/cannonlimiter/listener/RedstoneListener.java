package com.factions.cannonlimiter.listener;

import com.factions.cannonlimiter.chunk.ChunkLimiter;
import com.factions.cannonlimiter.registry.ChunkLimiterRegistry;
import com.factions.cannonlimiter.util.ChunkCoordinates;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

@RequiredArgsConstructor
public class RedstoneListener implements Listener {

    private final ChunkLimiterRegistry chunkLimiterRegistry;

    @EventHandler(priority = EventPriority.MONITOR)
    private void onRedstone(BlockRedstoneEvent event) {
        final Block block = event.getBlock();
        final Location location = block.getLocation();

        final Chunk chunk = location.getChunk();
        final ChunkCoordinates chunkCoordinates = new ChunkCoordinates(chunk.getX(), chunk.getZ());

        final ChunkLimiter chunkLimiter = chunkLimiterRegistry.getChunkLimiter(chunkCoordinates);

        final int newCurrent = event.getNewCurrent();
        if (newCurrent != 1) return;

        switch (block.getType()) {
            case REDSTONE_WIRE:
                chunkLimiter.increaseTicks(1);
                break;

            case DIODE:
            case REDSTONE_COMPARATOR:
                chunkLimiter.increaseTicks(2);
                break;

            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRedstoneActive(BlockRedstoneEvent event) {
        final Block block = event.getBlock();
        final Location location = block.getLocation();

        final Chunk chunk = location.getChunk();
        final ChunkCoordinates chunkCoordinates = new ChunkCoordinates(chunk.getX(), chunk.getZ());

        final ChunkLimiter chunkLimiter = chunkLimiterRegistry.getChunkLimiter(chunkCoordinates);
        if (chunkLimiter.getIntervalTicks() < 170) return;

        chunkLimiter.warn(chunk, "§cSistema de redstone foi quebrado pois alcançou " + chunkLimiter.getIntervalTicks() + " Ticks na chunk");

        event.setNewCurrent(0);

        block.setType(Material.AIR);
        block.getState().setType(Material.AIR);
        block.getState().update(true);
        chunkLimiter.setIntervalTicks(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onTNTSpawn(EntitySpawnEvent event) {
        final Entity entity = event.getEntity();
        if (entity.getType() != EntityType.PRIMED_TNT) return;

        final Location location = entity.getLocation();

        final Chunk chunk = location.getChunk();
        final ChunkCoordinates chunkCoordinates = new ChunkCoordinates(chunk.getX(), chunk.getZ());

        //todo verify if this method dont cause lag
        final ChunkLimiter chunkLimiter = chunkLimiterRegistry.getChunkLimiter(chunkCoordinates);
        if (chunkLimiter.getIntervalTicks() < 170) return;

        entity.remove();
        event.setCancelled(true);
    }
}
