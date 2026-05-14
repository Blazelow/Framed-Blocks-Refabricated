package xfacthd.framedblocks.client.model.compat;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;

import java.util.function.Function;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry<T>}.
 */
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>>
{
    BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker bakery,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState transform,
            ItemOverrides overrides
    );

    default void resolveParents(
            java.util.function.Function<net.minecraft.resources.ResourceLocation, UnbakedModel> modelGetter,
            IGeometryBakingContext context
    ) { }
}
