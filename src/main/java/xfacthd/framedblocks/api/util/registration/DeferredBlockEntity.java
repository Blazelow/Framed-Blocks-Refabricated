package xfacthd.framedblocks.api.util.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xfacthd.framedblocks.common.util.registration.RegistryHolder;

public final class DeferredBlockEntity<T extends BlockEntity>
        extends RegistryHolder<BlockEntityType<?>, BlockEntityType<T>>
{
    private DeferredBlockEntity(ResourceKey<BlockEntityType<?>> key)
    {
        super(key);
    }

    public static <T extends BlockEntity> DeferredBlockEntity<T> createBlockEntity(
            String namespace, String name
    )
    {
        return createBlockEntity(ResourceKey.create(
                Registries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(namespace, name)
        ));
    }

    public static <T extends BlockEntity> DeferredBlockEntity<T> createBlockEntity(
            ResourceKey<BlockEntityType<?>> key
    )
    {
        return new DeferredBlockEntity<>(key);
    }
}
