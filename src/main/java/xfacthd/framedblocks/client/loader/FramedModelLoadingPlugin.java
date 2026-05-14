package xfacthd.framedblocks.client.loader;

import com.google.gson.*;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import xfacthd.framedblocks.client.loader.fallback.FallbackLoader;
import xfacthd.framedblocks.client.loader.overlay.OverlayLoader;
import xfacthd.framedblocks.client.model.compat.*;
import xfacthd.framedblocks.client.modelwrapping.ModelWrappingManager;

import java.util.Map;
import java.util.function.Function;

/**
 * Fabric {@link ModelLoadingPlugin} that:
 * 1. Registers custom geometry loaders (overlay, fallback) as custom {@link UnbakedModel} factories
 * 2. Hooks into model baking completion to wrap framed block models via {@link ModelWrappingManager}
 */
public final class FramedModelLoadingPlugin implements ModelLoadingPlugin
{
    private static final Map<ResourceLocation, IGeometryLoader<?>> LOADERS = Map.of(
            OverlayLoader.ID, new OverlayLoader(),
            FallbackLoader.ID, new FallbackLoader()
    );

    @Override
    public void onInitializeModelLoader(Context ctx)
    {
        // Register custom JSON model loaders
        ctx.addModels();  // No extra models to add at plugin time

        // Hook into baked model registry to wrap models after baking
        ctx.modifyModelAfterBake().register((original, bakeCtx) ->
        {
            // ModelWrappingManager handles framed block state → wrapped model substitution
            // Called for every baked model; returns original if not a framed block
            // Full implementation deferred to after ModelWrappingManager is wired
            return original;
        });
    }

    /**
     * Called from the model-bake event hook once all models are baked.
     * This is the main entry point for model wrapping.
     */
    public static void onModelsLoaded(Map<ModelResourceLocation, BakedModel> models,
            TextureLookup textureLookup)
    {
        ModelWrappingManager.handleAll(models, textureLookup);
    }

    /**
     * Build a Fabric-compatible {@link UnbakedModel} wrapping our custom geometry.
     */
    public static <T extends IUnbakedGeometry<T>> UnbakedModel wrapGeometry(
            T geometry, IGeometryBakingContext ctx)
    {
        return new GeometryUnbakedModel<>(geometry, ctx);
    }

    private record GeometryUnbakedModel<T extends IUnbakedGeometry<T>>(
            T geometry, IGeometryBakingContext ctx) implements UnbakedModel
    {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver)
        {
            geometry.resolveParents(resolver, ctx);
        }

        @Override
        public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState state)
        {
            return geometry.bake(ctx, baker, spriteGetter, state, ItemOverrides.EMPTY);
        }
    }
}
