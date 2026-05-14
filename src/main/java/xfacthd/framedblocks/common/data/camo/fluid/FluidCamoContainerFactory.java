package xfacthd.framedblocks.common.data.camo.fluid;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.*;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.camo.CamoContainerFactory;
import xfacthd.framedblocks.api.camo.TriggerRegistrar;
import xfacthd.framedblocks.api.util.CamoMessageVerbosity;
import xfacthd.framedblocks.api.util.Utils;
import xfacthd.framedblocks.common.config.ServerConfig;

@SuppressWarnings("UnstableApiUsage") // Fabric Transfer API
public final class FluidCamoContainerFactory extends CamoContainerFactory<FluidCamoContainer>
{
    /** 1 bucket = 81,000 droplets in Fabric Transfer API */
    private static final long BUCKET_VOLUME = FluidConstants.BUCKET;

    private static final MapCodec<FluidCamoContainer> CODEC = BuiltInRegistries.FLUID.byNameCodec()
            .xmap(FluidCamoContainer::new, FluidCamoContainer::getFluid).fieldOf("fluid");
    private static final StreamCodec<RegistryFriendlyByteBuf, FluidCamoContainer> STREAM_CODEC =
            ByteBufCodecs.registry(Registries.FLUID)
                    .map(FluidCamoContainer::new, FluidCamoContainer::getFluid);

    @Override
    protected void writeToNetwork(CompoundTag tag, FluidCamoContainer container)
    {
        tag.putInt("fluid", BuiltInRegistries.FLUID.getId(container.getFluid()));
    }

    @Override
    protected FluidCamoContainer readFromNetwork(CompoundTag tag)
    {
        return new FluidCamoContainer(BuiltInRegistries.FLUID.byId(tag.getInt("fluid")));
    }

    @Override
    @Nullable
    public FluidCamoContainer applyCamo(Level level, BlockPos pos, Player player, ItemStack stack)
    {
        // Fabric Transfer API: look up fluid storage on item
        ContainerItemContext ctx = ContainerItemContext.ofPlayerHand(
                player,
                player.getMainHandItem() == stack
                        ? net.minecraft.world.InteractionHand.MAIN_HAND
                        : net.minecraft.world.InteractionHand.OFF_HAND
        );
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ctx);
        if (storage == null) return null;

        for (StorageView<FluidVariant> view : storage)
        {
            if (view.isResourceBlank() || view.getAmount() <= 0) continue;

            Fluid fluid = view.getResource().getFluid();
            if (!isValidFluid(fluid, player)) continue;
            // Reject fluids with extra NBT (components patch equivalent)
            if (!view.getResource().getComponents().isEmpty()) continue;

            if (!player.isCreative() && ServerConfig.VIEW.shouldConsumeCamoItem())
            {
                // Check we can drain exactly one bucket
                try (Transaction sim = Transaction.openOuter())
                {
                    long drained = storage.extract(view.getResource(), BUCKET_VOLUME, sim);
                    if (drained != BUCKET_VOLUME) continue;
                    // Don't commit — this was just a simulation
                }

                if (!level.isClientSide())
                {
                    try (Transaction tx = Transaction.openOuter())
                    {
                        long drained = storage.extract(view.getResource(), BUCKET_VOLUME, tx);
                        if (drained == BUCKET_VOLUME)
                        {
                            tx.commit();
                            // If the container became a different stack (e.g. bucket → empty bucket)
                            // ContainerItemContext handles the stack swap automatically
                        }
                    }
                }
            }

            return new FluidCamoContainer(fluid);
        }
        return null;
    }

    @Override
    public boolean removeCamo(Level level, BlockPos pos, Player player, ItemStack stack,
            FluidCamoContainer container)
    {
        if (stack.isEmpty()) return false;

        ContainerItemContext ctx = ContainerItemContext.ofPlayerHand(
                player,
                player.getMainHandItem() == stack
                        ? net.minecraft.world.InteractionHand.MAIN_HAND
                        : net.minecraft.world.InteractionHand.OFF_HAND
        );
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ctx);
        if (storage == null) return false;

        FluidVariant variant = FluidVariant.of(container.getFluid());

        if (!player.isCreative() && ServerConfig.VIEW.shouldConsumeCamoItem())
        {
            try (Transaction sim = Transaction.openOuter())
            {
                long inserted = storage.insert(variant, BUCKET_VOLUME, sim);
                if (inserted != BUCKET_VOLUME) return false;
            }

            if (!level.isClientSide())
            {
                try (Transaction tx = Transaction.openOuter())
                {
                    long inserted = storage.insert(variant, BUCKET_VOLUME, tx);
                    if (inserted == BUCKET_VOLUME)
                    {
                        tx.commit();
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean canTriviallyConvertToItemStack() { return false; }

    @Override
    public ItemStack dropCamo(FluidCamoContainer container) { return ItemStack.EMPTY; }

    @Override
    public boolean validateCamo(FluidCamoContainer container)
    {
        return isValidFluid(container.getFluid(), null);
    }

    private static boolean isValidFluid(Fluid fluid, @Nullable Player player)
    {
        if (fluid == Fluids.EMPTY)
        {
            return false;
        }
        if (BuiltInRegistries.FLUID.wrapAsHolder(fluid).is(Utils.FLUID_BLACKLIST))
        {
            displayValidationMessage(player, MSG_BLACKLISTED, CamoMessageVerbosity.DEFAULT);
            return false;
        }
        return true;
    }

    @Override
    public MapCodec<FluidCamoContainer> codec() { return CODEC; }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, FluidCamoContainer> streamCodec()
    {
        return STREAM_CODEC;
    }

    @Override
    public void registerTriggerItems(TriggerRegistrar registrar) { }
}
