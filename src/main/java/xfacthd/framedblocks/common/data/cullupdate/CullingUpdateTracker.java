package xfacthd.framedblocks.common.data.cullupdate;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import xfacthd.framedblocks.common.net.payload.ClientboundCullingUpdatePayload;

import java.util.Map;

public final class CullingUpdateTracker
{
    private static final Map<ResourceKey<Level>, Long2ObjectMap<LongSet>> UPDATED_POSITIONS = new Reference2ObjectOpenHashMap<>();

    /**
     * Register Fabric event listeners for tick-based cull update dispatch.
     * Called from {@link xfacthd.framedblocks.FramedBlocks#onInitialize()}.
     */
    public static void register()
    {
        // Tick all loaded server worlds; send pending culling updates at start of tick
        ServerWorldEvents.LOAD.register((server, world) -> { }); // ensure we're aware of new levels
        // Use the server tick event — iterate over all loaded server levels
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.START_SERVER_TICK.register(server ->
        {
            for (ServerLevel level : server.getAllLevels())
            {
                tickLevel(level);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> UPDATED_POSITIONS.clear());
    }

    // Send updates at the start of the next tick to ensure receipt after block update packet
    private static void tickLevel(ServerLevel level)
    {
        ResourceKey<Level> dim = level.dimension();
        Long2ObjectMap<LongSet> chunks = UPDATED_POSITIONS.get(dim);
        if (chunks == null || chunks.isEmpty())
        {
            return;
        }

        for (Long2ObjectMap.Entry<LongSet> entry : chunks.long2ObjectEntrySet())
        {
            long chunkKey = entry.getLongKey();
            ChunkPos chunkPos = new ChunkPos(chunkKey);
            ClientboundCullingUpdatePayload payload = new ClientboundCullingUpdatePayload(chunkKey, entry.getValue());

            for (net.minecraft.server.level.ServerPlayer player : PlayerLookup.tracking(level, chunkPos))
            {
                ServerPlayNetworking.send(player, payload);
            }
        }
        chunks.clear();
    }

    public static void enqueueCullingUpdate(Level level, BlockPos pos)
    {
        UPDATED_POSITIONS.computeIfAbsent(level.dimension(), $ -> new Long2ObjectOpenHashMap<>())
                .computeIfAbsent(ChunkPos.asLong(pos), $ -> new LongArraySet())
                .add(pos.asLong());
    }

    public static void onServerShutdown()
    {
        UPDATED_POSITIONS.clear();
    }



    private CullingUpdateTracker() { }
}
