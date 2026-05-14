package xfacthd.framedblocks.api.util.registration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xfacthd.framedblocks.api.blueprint.AuxBlueprintData;
import xfacthd.framedblocks.api.util.FramedConstants;
import xfacthd.framedblocks.common.util.registration.RegistryHolder;

public final class DeferredAuxDataType<T extends AuxBlueprintData<T>>
        extends RegistryHolder<AuxBlueprintData.Type<?>, AuxBlueprintData.Type<T>>
{
    private DeferredAuxDataType(ResourceKey<AuxBlueprintData.Type<?>> key)
    {
        super(key);
    }

    public static <T extends AuxBlueprintData<T>> DeferredAuxDataType<T> createAuxBlueprintDataType(
            String namespace, String name
    )
    {
        return createAuxBlueprintDataType(ResourceKey.create(
                FramedConstants.AUX_BLUEPRINT_DATA_TYPE_REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(namespace, name)
        ));
    }

    public static <T extends AuxBlueprintData<T>> DeferredAuxDataType<T> createAuxBlueprintDataType(
            ResourceKey<AuxBlueprintData.Type<?>> key
    )
    {
        return new DeferredAuxDataType<>(key);
    }
}
