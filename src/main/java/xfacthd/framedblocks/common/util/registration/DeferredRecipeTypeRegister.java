package xfacthd.framedblocks.common.util.registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import xfacthd.framedblocks.api.util.registration.DeferredRecipeType;

public final class DeferredRecipeTypeRegister
{
    private final FabricRegistrar<RecipeType<?>> registrar;

    private DeferredRecipeTypeRegister(String namespace)
    {
        this.registrar = FabricRegistrar.create(Registries.RECIPE_TYPE, namespace);
    }

    @SuppressWarnings("unchecked")
    public <R extends Recipe<?>> DeferredRecipeType<R> registerRecipeType(String name)
    {
        DeferredRecipeType<R> holder = DeferredRecipeType.createRecipeType(registrar.getNamespace(), name);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(registrar.getNamespace(), name);
        registrar.register(
                (RegistryHolder<RecipeType<?>, RecipeType<R>>) (RegistryHolder<?, ?>) holder,
                () -> RecipeType.simple(location)
        );
        return holder;
    }

    public void registerAll()
    {
        registrar.registerAll(BuiltInRegistries.RECIPE_TYPE);
    }

    public static DeferredRecipeTypeRegister create(String namespace)
    {
        return new DeferredRecipeTypeRegister(namespace);
    }
}
