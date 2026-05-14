package xfacthd.framedblocks.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;
import xfacthd.framedblocks.common.config.ServerConfig;
import xfacthd.framedblocks.common.crafting.FramingSawRecipeCache;

public final class EventHandler
{
    /**
     * Register all server-side Fabric event listeners.
     * Called from {@link xfacthd.framedblocks.FramedBlocks#onInitialize()}.
     */
    public static void register()
    {
        // Left-click block → handle framed block interactions and intangibility
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
        {
            Level level = (Level) world;
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof IFramedBlock block)
            {
                if (block.handleBlockLeftClick(state, level, pos, player))
                {
                    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && level.isClientSide())
                    {
                        // Client-side: destroy delay reset handled via ClientEventHandler
                        xfacthd.framedblocks.client.util.ClientAccess.resetDestroyDelay();
                    }
                    return InteractionResult.FAIL; // cancel vanilla mining
                }

                if (ServerConfig.VIEW.enableIntangibility() && block.getBlockType().allowMakingIntangible())
                {
                    if (level.getBlockEntity(pos) instanceof FramedBlockEntity be && be.isIntangible(null))
                    {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    /** Called on server stop to clear recipe caches. */
    public static void onServerShutdown()
    {
        FramingSawRecipeCache.get(false).clear();
    }



    private EventHandler() { }
}
