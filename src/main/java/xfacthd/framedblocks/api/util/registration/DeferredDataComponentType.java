package xfacthd.framedblocks.api.util.registration;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xfacthd.framedblocks.common.util.registration.RegistryHolder;

public final class DeferredDataComponentType<T>
        extends RegistryHolder<DataComponentType<?>, DataComponentType<T>>
{
    private DeferredDataComponentType(ResourceKey<DataComponentType<?>> key)
    {
        super(key);
    }

    public static <T> DeferredDataComponentType<T> createDataComponent(
            String namespace, String name
    )
    {
        return createDataComponent(ResourceKey.create(
                Registries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(namespace, name)
        ));
    }

    public static <T> DeferredDataComponentType<T> createDataComponent(
            ResourceKey<DataComponentType<?>> key
    )
    {
        return new DeferredDataComponentType<>(key);
    }
}
