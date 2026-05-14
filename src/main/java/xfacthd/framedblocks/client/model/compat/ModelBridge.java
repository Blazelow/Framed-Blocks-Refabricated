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
 * Adapts a vanilla {@link BakedModel} to {@link FBakedModel}.
 * Returns all quads regardless of layer (vanilla models don't filter by render type).
 */
record ModelBridge(BakedModel model) implements FBakedModel
{
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            RandomSource rand, ModelData data, @Nullable RenderType layer)
    {
        // Vanilla models don't support per-layer or per-data quad filtering
        return model.getQuads(state, side, rand);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data)
    {
        // Guess render type from vanilla ItemBlockRenderTypes
        return ChunkRenderTypeSet.of(
                net.minecraft.client.renderer.ItemBlockRenderTypes.getRenderType(state, false)
        );
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData)
    {
        return modelData;
    }
}
