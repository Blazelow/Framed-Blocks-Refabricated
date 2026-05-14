package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.IQuadTransformer}.
 */
@FunctionalInterface
public interface IQuadTransformer
{
    /** Process a single quad, returning the transformed version (may return same instance). */
    BakedQuad process(BakedQuad quad);

    /** Transform a list of quads. */
    default List<BakedQuad> process(List<BakedQuad> quads)
    {
        if (quads.isEmpty()) return quads;
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) result.add(process(quad));
        return result;
    }

    /** Compose two transformers: apply {@code this} then {@code other}. */
    default IQuadTransformer andThen(IQuadTransformer other)
    {
        return quad -> other.process(this.process(quad));
    }

    static IQuadTransformer of(UnaryOperator<BakedQuad> op)
    {
        return op::apply;
    }
}
