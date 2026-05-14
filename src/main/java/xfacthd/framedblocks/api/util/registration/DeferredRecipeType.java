package xfacthd.framedblocks.api.util.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import xfacthd.framedblocks.common.util.registration.RegistryHolder;

public final class DeferredRecipeType<T extends Recipe<?>>
        extends RegistryHolder<RecipeType<?>, RecipeType<T>>
{
    private DeferredRecipeType(ResourceKey<RecipeType<?>> key)
    {
        super(key);
    }

    public static <T extends Recipe<?>> DeferredRecipeType<T> createRecipeType(
            String namespace, String name
    )
    {
        return createRecipeType(ResourceKey.create(
                Registries.RECIPE_TYPE,
                ResourceLocation.fromNamespaceAndPath(namespace, name)
        ));
    }

    public static <T extends Recipe<?>> DeferredRecipeType<T> createRecipeType(
            ResourceKey<RecipeType<?>> key
    )
    {
        return new DeferredRecipeType<>(key);
    }
}
