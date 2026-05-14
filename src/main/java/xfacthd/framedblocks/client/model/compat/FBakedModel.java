package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Extension interface for {@link BakedModel} providing the NeoForge-equivalent
 * data-aware methods that the framed block model system relies on.
 * <p>
 * All framed block models ({@link BakedModelWrapper} subclasses) implement this.
 * For external vanilla/other-mod models, use {@link ModelBridge} to adapt them.
 */
public interface FBakedModel
{
    /**
     * Get quads for the given render type and model data.
     * Replaces NeoForge's {@code BakedModel.getQuads(state, side, rand, ModelData, RenderType)}.
     */
    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            RandomSource rand, ModelData data, @Nullable RenderType layer);

    /**
     * Get render types for this model.
     * Replaces NeoForge's {@code BakedModel.getRenderTypes(state, rand, ModelData)}.
     */
    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data)
    {
        return ModelUtils.CUTOUT;
    }

    /**
     * Get model data for a block position.
     * Replaces NeoForge's {@code BakedModel.getModelData(level, pos, state, ModelData)}.
     */
    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData)
    {
        return modelData;
    }

    /**
     * Adapt a vanilla {@link BakedModel} to {@link FBakedModel}.
     * Falls back to calling vanilla {@code getQuads()} without layer/data filtering.
     */
    static FBakedModel of(BakedModel model)
    {
        if (model instanceof FBakedModel fb) return fb;
        return new ModelBridge(model);
    }
}
