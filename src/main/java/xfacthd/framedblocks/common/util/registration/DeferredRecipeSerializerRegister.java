package xfacthd.framedblocks.common.util.registration;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import xfacthd.framedblocks.api.util.registration.DeferredRecipeSerializer;
import xfacthd.framedblocks.common.util.SimpleRecipeSerializer;

public final class DeferredRecipeSerializerRegister
{
    private final FabricRegistrar<RecipeSerializer<?>> registrar;

    private DeferredRecipeSerializerRegister(String namespace)
    {
        this.registrar = FabricRegistrar.create(Registries.RECIPE_SERIALIZER, namespace);
    }

    @SuppressWarnings("unchecked")
    public <R extends Recipe<?>> DeferredRecipeSerializer<R> registerRecipeSerializer(
            String name, MapCodec<R> codec, StreamCodec<RegistryFriendlyByteBuf, R> streamCodec
    )
    {
        DeferredRecipeSerializer<R> holder = DeferredRecipeSerializer.createRecipeSerializer(
                registrar.getNamespace(), name
        );
        registrar.register(
                (RegistryHolder<RecipeSerializer<?>, RecipeSerializer<R>>) (RegistryHolder<?, ?>) holder,
                () -> new SimpleRecipeSerializer<>(codec, streamCodec)
        );
        return holder;
    }

    public void registerAll()
    {
        registrar.registerAll(BuiltInRegistries.RECIPE_SERIALIZER);
    }

    public static DeferredRecipeSerializerRegister create(String namespace)
    {
        return new DeferredRecipeSerializerRegister(namespace);
    }
}
