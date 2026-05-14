package xfacthd.framedblocks.common.util.registration;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import xfacthd.framedblocks.api.util.registration.DeferredDataComponentType;

import java.util.function.UnaryOperator;

public final class DeferredDataComponentTypeRegister
{
    private final FabricRegistrar<DataComponentType<?>> registrar;

    private DeferredDataComponentTypeRegister(String namespace)
    {
        this.registrar = FabricRegistrar.create(Registries.DATA_COMPONENT_TYPE, namespace);
    }

    @SuppressWarnings("unchecked")
    public <D> DeferredDataComponentType<D> registerComponentType(
            String name, UnaryOperator<DataComponentType.Builder<D>> builder
    )
    {
        DeferredDataComponentType<D> holder = DeferredDataComponentType.createDataComponent(
                registrar.getNamespace(), name
        );
        registrar.register(
                (RegistryHolder<DataComponentType<?>, DataComponentType<D>>) (RegistryHolder<?, ?>) holder,
                () -> builder.apply(DataComponentType.builder()).build()
        );
        return holder;
    }

    public void registerAll()
    {
        registrar.registerAll(BuiltInRegistries.DATA_COMPONENT_TYPE);
    }

    public static DeferredDataComponentTypeRegister create(String namespace)
    {
        return new DeferredDataComponentTypeRegister(namespace);
    }
}
