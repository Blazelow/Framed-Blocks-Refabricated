package xfacthd.framedblocks.client.model.cube;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.client.model.compat.IQuadTransformer;
import xfacthd.framedblocks.client.model.compat.QuadTransformers;
import xfacthd.framedblocks.client.model.compat.ModelData;
import xfacthd.framedblocks.api.model.wrapping.GeometryFactory;

import java.util.*;

public class FramedGlowingCubeGeometry extends FramedCubeGeometry
{
    private static final IQuadTransformer FULLBRIGHT_TRANSFORMER = QuadTransformers.settingMaxEmissivity();

    public FramedGlowingCubeGeometry(GeometryFactory.Context ctx)
    {
        super(ctx);
    }

    @Override
    public void postProcessUncachedQuads(List<BakedQuad> quads)
    {
        quads.replaceAll(quad ->
        {
            int[] vertexData = quad.getVertices();
            BakedQuad newQuad = new BakedQuad(
                    Arrays.copyOf(vertexData, vertexData.length),
                    quad.getTintIndex(),
                    quad.getDirection(),
                    quad.getSprite(),
                    false,
                    false
            );
            FULLBRIGHT_TRANSFORMER.processInPlace(newQuad);
            return newQuad;
        });
    }

    @Override
    public boolean hasUncachedPostProcessing()
    {
        return true;
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType)
    {
        return TriState.FALSE;
    }
}
