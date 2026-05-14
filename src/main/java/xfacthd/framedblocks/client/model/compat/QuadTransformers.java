package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.QuadTransformers}.
 * Provides common quad transformation utilities.
 */
public final class QuadTransformers
{
    private static final IQuadTransformer EMISSIVE;

    static
    {
        // Make a quad fully emissive by setting max light coords
        // BakedQuad stores vertex data as int[]; light data is at offset 6 per vertex (4 ints per vertex, 4 vertices)
        // Vanilla format: pos(3) + color(1) + uv(2) + light(2) + normal(1) = 9 ints per vertex? Actually 8.
        // For simplicity and correctness, we store a flag and handle in the renderer via FRAPI if available.
        // Otherwise, emissivity is best handled via block model JSON "shade":false + LightTexture.FULL_BRIGHT.
        // For now: no-op transformer; full emissivity requires FRAPI Renderer API (Step 7 extended).
        EMISSIVE = quad -> quad;
    }

    /**
     * Returns a transformer that sets maximum emissivity on quads.
     * Full implementation requires the Fabric Rendering API (FRAPI).
     * TODO (Step 7 extended): Use FRAPI QuadEmitter.lightmap(LightTexture.FULL_BRIGHT) for true emissivity.
     */
    public static IQuadTransformer settingMaxEmissivity()
    {
        return EMISSIVE;
    }



    private QuadTransformers() { }
}
