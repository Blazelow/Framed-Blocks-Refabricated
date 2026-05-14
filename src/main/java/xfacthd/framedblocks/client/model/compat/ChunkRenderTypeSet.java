package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.RenderType;

import java.util.*;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.ChunkRenderTypeSet}.
 * An immutable ordered set of {@link RenderType}s used to specify which render layers
 * a block model should be rendered in.
 */
public final class ChunkRenderTypeSet implements Iterable<RenderType>
{
    private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(List.of());

    private final List<RenderType> types;

    private ChunkRenderTypeSet(List<RenderType> types)
    {
        this.types = List.copyOf(types);
    }

    public static ChunkRenderTypeSet none()
    {
        return NONE;
    }

    public static ChunkRenderTypeSet of(RenderType... types)
    {
        if (types.length == 0) return NONE;
        return new ChunkRenderTypeSet(Arrays.asList(types));
    }

    public static ChunkRenderTypeSet of(Collection<RenderType> types)
    {
        if (types.isEmpty()) return NONE;
        return new ChunkRenderTypeSet(new ArrayList<>(types));
    }

    /** Union of two sets. */
    public static ChunkRenderTypeSet union(ChunkRenderTypeSet a, ChunkRenderTypeSet b)
    {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        LinkedHashSet<RenderType> merged = new LinkedHashSet<>(a.types);
        merged.addAll(b.types);
        return new ChunkRenderTypeSet(new ArrayList<>(merged));
    }

    public boolean isEmpty()
    {
        return types.isEmpty();
    }

    public boolean contains(RenderType type)
    {
        return types.contains(type);
    }

    public List<RenderType> asList()
    {
        return types;
    }

    @Override
    public Iterator<RenderType> iterator()
    {
        return types.iterator();
    }

    @Override
    public String toString()
    {
        return "ChunkRenderTypeSet" + types;
    }
}
