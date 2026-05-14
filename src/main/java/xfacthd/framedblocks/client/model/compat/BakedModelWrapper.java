package xfacthd.framedblocks.client.model.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.BakedModelWrapper<T>}.
 * A delegating wrapper around a {@link BakedModel} that forwards all calls to the wrapped model.
 * Subclasses override only the methods they need to change.
 */
public abstract class BakedModelWrapper<T extends BakedModel> implements BakedModel
{
    protected final T wrapped;

    protected BakedModelWrapper(T wrapped)
    {
        this.wrapped = wrapped;
    }

    public T getOriginalModel()
    {
        return wrapped;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
            RandomSource rand, ModelData data, @Nullable RenderType layer)
    {
        return wrapped.getQuads(state, side, rand, data, layer);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)
    {
        return wrapped.getQuads(state, side, rand);
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight()
    {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer()
    {
        return wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return wrapped.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data)
    {
        return wrapped.getParticleIcon(data);
    }

    @Override
    public ItemTransforms getTransforms()
    {
        return wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides()
    {
        return wrapped.getOverrides();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext ctx, PoseStack poseStack, boolean leftHand)
    {
        return wrapped.applyTransform(ctx, poseStack, leftHand);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean cull)
    {
        return wrapped.getRenderPasses(stack, cull);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data)
    {
        return wrapped.getRenderTypes(state, rand, data);
    }

    @Override
    public ModelData getModelData(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos,
            BlockState state, ModelData modelData)
    {
        return wrapped.getModelData(level, pos, state, modelData);
    }
}
