package xfacthd.framedblocks.common.util.registration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xfacthd.framedblocks.api.util.registration.DeferredBlockEntity;

import java.util.function.Supplier;

public final class DeferredBlockEntityRegister
{
    private final FabricRegistrar<BlockEntityType<?>> registrar;

    private DeferredBlockEntityRegister(String namespace)
    {
        this.registrar = FabricRegistrar.create(Registries.BLOCK_ENTITY_TYPE, namespace);
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> DeferredBlockEntity<T> registerBlockEntity(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<Block[]> blocks
    )
    {
        DeferredBlockEntity<T> holder = DeferredBlockEntity.createBlockEntity(registrar.getNamespace(), name);
        registrar.register(
                (RegistryHolder<BlockEntityType<?>, BlockEntityType<T>>) (RegistryHolder<?, ?>) holder,
                () -> BlockEntityType.Builder.of(factory, blocks.get()).build(null)
        );
        return holder;
    }

    public void registerAll()
    {
        registrar.registerAll(BuiltInRegistries.BLOCK_ENTITY_TYPE);
    }

    public static DeferredBlockEntityRegister create(String namespace)
    {
        return new DeferredBlockEntityRegister(namespace);
    }
}
