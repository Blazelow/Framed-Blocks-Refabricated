package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext}.
 * Provides context to geometry baking, primarily access to the owner {@link BlockModel}.
 */
public interface IGeometryBakingContext
{
    String getModelName();
    boolean isGui3d();
    boolean useBlockLight();
    boolean useAmbientOcclusion();
    TextureAtlasSprite getParticleIcon();
    net.minecraft.client.renderer.block.model.ItemTransforms getTransforms();
    ModelState getRootTransform();
}
