package xfacthd.framedblocks.common.util.registration;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import xfacthd.framedblocks.api.blueprint.AuxBlueprintData;
import xfacthd.framedblocks.api.util.FramedConstants;
import xfacthd.framedblocks.api.util.registration.DeferredAuxDataType;

public final class DeferredAuxDataTypeRegister
{
    private final FabricRegistrar<AuxBlueprintData.Type<?>> registrar;
    private Registry<AuxBlueprintData.Type<?>> registry;

    private DeferredAuxDataTypeRegister(String namespace)
    {
        this.registrar = FabricRegistrar.create(FramedConstants.AUX_BLUEPRINT_DATA_TYPE_REGISTRY_KEY, namespace);
    }

    @SuppressWarnings("unchecked")
    public <T extends AuxBlueprintData<T>> DeferredAuxDataType<T> registerAuxDataType(
            String name,
            MapCodec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
    )
    {
        DeferredAuxDataType<T> holder = DeferredAuxDataType.createAuxBlueprintDataType(
                registrar.getNamespace(), name
        );
        registrar.register(
                (RegistryHolder<AuxBlueprintData.Type<?>, AuxBlueprintData.Type<T>>) (RegistryHolder<?, ?>) holder,
                () -> new AuxBlueprintData.Type<>(codec, streamCodec)
        );
        return holder;
    }

    /** Must be called with the already-built custom registry after it is created in FBContent. */
    public void registerAll(Registry<AuxBlueprintData.Type<?>> registry)
    {
        this.registry = registry;
        registrar.registerAll(registry);
    }

    public static DeferredAuxDataTypeRegister create(String namespace)
    {
        return new DeferredAuxDataTypeRegister(namespace);
    }
}
