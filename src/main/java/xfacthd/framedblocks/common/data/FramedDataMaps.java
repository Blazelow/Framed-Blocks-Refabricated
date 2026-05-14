package xfacthd.framedblocks.common.data;

import xfacthd.framedblocks.common.data.camo.block.rotator.BlockCamoRotators;

/**
 * NeoForge DataMapType system stub.
 *
 * TODO (Step 8 - Compat / Data): NeoForge's {@code DataMapType<Block, BlockCamoRotatorPrototype>}
 *  attaches per-entry data to registry entries via data packs.
 *  On Fabric, the equivalent is a custom data-pack driven JSON resource loaded via
 *  {@code ResourceManagerHelper} + a custom {@code SimpleSynchronousResourceReloadListener}.
 *
 *  For now, {@link BlockCamoRotators} will be empty (no rotators configured from data packs)
 *  until this is properly ported.
 */
public final class FramedDataMaps
{
    /**
     * Called from {@link xfacthd.framedblocks.FramedBlocks#onInitialize()} to register any
     * data-driven reloaders. Currently a no-op stub.
     */
    public static void init()
    {
        // TODO (Step 8): Register a ResourceManagerHelper reload listener that reads
        //   data/framedblocks/block_camo_rotators/*.json and populates BlockCamoRotators
    }

    /**
     * Called when resources are reloaded. Clears and repopulates BlockCamoRotators.
     */
    public static void onDataReloaded()
    {
        BlockCamoRotators.reload();
    }



    private FramedDataMaps() { }
}
