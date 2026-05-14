package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.RenderType;

/**
 * Forward reference to keep compat package self-contained.
 * Actual constants live in {@link xfacthd.framedblocks.api.model.util.ModelUtils}.
 */
final class ModelUtils
{
    static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());

    private ModelUtils() { }
}
