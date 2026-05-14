package xfacthd.framedblocks.common.data.capabilities;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import team.reborn.energy.api.EnergyStorage;
import xfacthd.framedblocks.common.FBContent;
import xfacthd.framedblocks.common.blockentity.special.*;
import xfacthd.framedblocks.common.capability.*;
import xfacthd.framedblocks.common.data.component.FluidContent;
import xfacthd.framedblocks.common.capability.IItemHandler;
import xfacthd.framedblocks.common.capability.IEnergyStorage;
import xfacthd.framedblocks.common.capability.TankFluidHandler;

/**
 * Registers block entity / item capabilities with the Fabric Transfer API.
 * Replaces NeoForge's {@code RegisterCapabilitiesEvent}.
 *
 * Called from {@link xfacthd.framedblocks.FramedBlocks#onInitialize()}.
 */
@SuppressWarnings("UnstableApiUsage") // Fabric Transfer API is @Experimental but stable in practice
public final class CapabilitySetup
{
    public static void register()
    {
        registerItemHandlers();
        registerEnergyHandlers();
        registerFluidHandlers();
    }

    // ---- Item storage ----

    private static void registerItemHandlers()
    {
        // Framed Secret Storage
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> wrapItemHandler(be.getItemHandler()),
                FBContent.BE_TYPE_FRAMED_SECRET_STORAGE.value()
        );

        // Framed Chest
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> wrapItemHandler(be.getChestItemHandler(true)),
                FBContent.BE_TYPE_FRAMED_CHEST.value()
        );

        // Powered Framing Saw — expose items from all sides except top
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> side != Direction.UP ? wrapItemHandler(be.getExternalItemHandler()) : null,
                FBContent.BE_TYPE_POWERED_FRAMING_SAW.value()
        );

        // Framed Chiseled Bookshelf
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> wrapItemHandler(be.getItemHandler()),
                FBContent.BE_TYPE_FRAMED_CHISELED_BOOKSHELF.value()
        );

        // Framed Hopper
        ItemStorage.SIDED.registerForBlockEntity(
                (be, side) -> wrapItemHandler(be.new ItemHandler()),
                FBContent.BE_TYPE_FRAMED_HOPPER.value()
        );
    }

    // ---- Energy storage ----

    private static void registerEnergyHandlers()
    {
        // Powered Framing Saw — expose energy from all sides except top
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, side) -> side != Direction.UP ? wrapEnergyStorage(be.getEnergyStorage()) : null,
                FBContent.BE_TYPE_POWERED_FRAMING_SAW.value()
        );
    }

    // ---- Fluid storage ----

    private static void registerFluidHandlers()
    {
        // Framed Tank — block fluid storage
        FluidStorage.SIDED.registerForBlockEntity(
                (be, side) -> wrapFluidHandler(be.getFluidHandler()),
                FBContent.BE_TYPE_FRAMED_TANK.value()
        );

        // Framed Tank item — allows filling/draining the tank item with fluid-carrying items
        FluidStorage.ITEM.registerForItems(
                (stack, context) -> new TankItemFluidStorage(stack, context),
                FBContent.BLOCK_FRAMED_TANK.value().asItem()
        );
    }

    // ---- Transfer API bridges ----

    /**
     * Wraps our internal {@link IItemHandler} as a Fabric Transfer API {@link Storage}.
     */
    private static Storage<ItemVariant> wrapItemHandler(IItemHandler handler)
    {
        if (handler == null) return null;
        return new IItemHandlerStorage(handler);
    }

    /**
     * Wraps our internal {@link IEnergyStorage} as a Team Reborn {@link EnergyStorage}.
     */
    private static EnergyStorage wrapEnergyStorage(IEnergyStorage storage)
    {
        if (storage == null) return null;
        return new IEnergyStorageWrapper(storage);
    }

    /**
     * Wraps our internal {@link TankFluidHandler} as a Fabric Transfer API fluid {@link Storage}.
     */
    private static Storage<FluidVariant> wrapFluidHandler(TankFluidHandler handler)
    {
        if (handler == null) return null;
        return new TankFluidStorage(handler);
    }

    // ---- Inner bridge classes ----

    /** Adapts {@link IItemHandler} to Fabric Transfer API {@code Storage<ItemVariant>}. */
    private record IItemHandlerStorage(IItemHandler handler) implements Storage<ItemVariant>
    {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            long inserted = 0;
            for (int slot = 0; slot < handler.getSlots(); slot++)
            {
                int toInsert = (int) Math.min(maxAmount - inserted, Integer.MAX_VALUE);
                if (toInsert <= 0) break;
                var stack = resource.toStack(toInsert);
                var remainder = handler.insertItem(slot, stack, true);
                int accepted = toInsert - remainder.getCount();
                if (accepted > 0)
                {
                    transaction.addCloseCallback((t, result) ->
                    {
                        if (result.wasCommitted())
                        {
                            handler.insertItem(slot, resource.toStack((int)(maxAmount - inserted)), false);
                        }
                    });
                    inserted += accepted;
                }
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            long extracted = 0;
            for (int slot = 0; slot < handler.getSlots(); slot++)
            {
                int toExtract = (int) Math.min(maxAmount - extracted, Integer.MAX_VALUE);
                if (toExtract <= 0) break;
                var stack = handler.extractItem(slot, toExtract, true);
                if (!stack.isEmpty() && ItemVariant.of(stack).equals(resource))
                {
                    int count = stack.getCount();
                    final int slotCopy = slot;
                    transaction.addCloseCallback((t, result) ->
                    {
                        if (result.wasCommitted()) handler.extractItem(slotCopy, count, false);
                    });
                    extracted += count;
                }
            }
            return extracted;
        }

        @Override
        public java.util.Iterator<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> iterator()
        {
            return new java.util.Iterator<>()
            {
                int slot = 0;
                @Override public boolean hasNext() { return slot < handler.getSlots(); }
                @Override public net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> next()
                {
                    final int s = slot++;
                    return new net.fabricmc.fabric.api.transfer.v1.storage.StorageView<>()
                    {
                        @Override public ItemVariant getResource() { return ItemVariant.of(handler.getStackInSlot(s)); }
                        @Override public long getAmount() { return handler.getStackInSlot(s).getCount(); }
                        @Override public long getCapacity() { return handler.getSlotLimit(s); }
                        @Override public boolean isResourceBlank() { return handler.getStackInSlot(s).isEmpty(); }
                        @Override public long extract(ItemVariant r, long max, TransactionContext tx) { return 0; }
                    };
                }
            };
        }
    }

    /** Adapts {@link IEnergyStorage} to Team Reborn {@link EnergyStorage}. */
    private record IEnergyStorageWrapper(IEnergyStorage storage) implements EnergyStorage
    {
        @Override
        public long insert(long maxAmount, TransactionContext transaction)
        {
            int received = storage.receiveEnergy((int) Math.min(maxAmount, Integer.MAX_VALUE), true);
            if (received > 0)
            {
                transaction.addCloseCallback((t, result) ->
                {
                    if (result.wasCommitted()) storage.receiveEnergy(received, false);
                });
            }
            return received;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction)
        {
            int extracted = storage.extractEnergy((int) Math.min(maxAmount, Integer.MAX_VALUE), true);
            if (extracted > 0)
            {
                transaction.addCloseCallback((t, result) ->
                {
                    if (result.wasCommitted()) storage.extractEnergy(extracted, false);
                });
            }
            return extracted;
        }

        @Override
        public long getAmount() { return storage.getEnergyStored(); }

        @Override
        public long getCapacity() { return storage.getMaxEnergyStored(); }

        @Override
        public boolean supportsInsertion() { return storage.canReceive(); }

        @Override
        public boolean supportsExtraction() { return storage.canExtract(); }
    }

    /** Adapts {@link TankFluidHandler} to Fabric Transfer API {@code Storage<FluidVariant>}. */
    private record TankFluidStorage(TankFluidHandler handler)
            implements SingleSlotStorage<FluidVariant>
    {
        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction)
        {
            long accepted = handler.fill(resource.getFluid(), maxAmount, true);
            if (accepted > 0)
            {
                transaction.addCloseCallback((t, result) ->
                {
                    if (result.wasCommitted()) handler.fill(resource.getFluid(), accepted, false);
                });
            }
            return accepted;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
        {
            long extracted = handler.drain(resource.getFluid(), maxAmount, true);
            if (extracted > 0)
            {
                transaction.addCloseCallback((t, result) ->
                {
                    if (result.wasCommitted()) handler.drain(resource.getFluid(), extracted, false);
                });
            }
            return extracted;
        }

        @Override
        public boolean isResourceBlank() { return handler.isEmpty(); }

        @Override
        public FluidVariant getResource()
        {
            return handler.isEmpty() ? FluidVariant.blank() : FluidVariant.of(handler.getFluid());
        }

        @Override
        public long getAmount() { return handler.getAmount(); }

        @Override
        public long getCapacity() { return TankFluidHandler.CAPACITY; }
    }

    /**
     * Item-context fluid storage for the tank item.
     * Allows filling/draining the framed tank item stack.
     */
    private static final class TankItemFluidStorage implements SingleSlotStorage<FluidVariant>
    {
        private final net.minecraft.world.item.ItemStack stack;
        private final net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext context;

        TankItemFluidStorage(net.minecraft.world.item.ItemStack stack,
                net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext context)
        {
            this.stack = stack;
            this.context = context;
        }

        private FluidContent getContent()
        {
            return stack.getOrDefault(FBContent.DC_TYPE_TANK_CONTENTS.value(), FluidContent.EMPTY);
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction)
        {
            FluidContent current = getContent();
            if (!current.isEmpty() && current.fluid() != resource.getFluid()) return 0;
            long space = TankFluidHandler.CAPACITY - current.amount();
            long toFill = Math.min(space, maxAmount);
            if (toFill <= 0) return 0;

            transaction.addCloseCallback((t, result) ->
            {
                if (result.wasCommitted())
                {
                    FluidContent updated = new FluidContent(resource.getFluid(), current.amount() + toFill);
                    stack.set(FBContent.DC_TYPE_TANK_CONTENTS.value(), updated);
                }
            });
            return toFill;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
        {
            FluidContent current = getContent();
            if (current.isEmpty() || current.fluid() != resource.getFluid()) return 0;
            long toDrain = Math.min(current.amount(), maxAmount);
            if (toDrain <= 0) return 0;

            transaction.addCloseCallback((t, result) ->
            {
                if (result.wasCommitted())
                {
                    long remaining = current.amount() - toDrain;
                    if (remaining <= 0)
                        stack.remove(FBContent.DC_TYPE_TANK_CONTENTS.value());
                    else
                        stack.set(FBContent.DC_TYPE_TANK_CONTENTS.value(),
                                new FluidContent(current.fluid(), remaining));
                }
            });
            return toDrain;
        }

        @Override public boolean isResourceBlank() { return getContent().isEmpty(); }
        @Override public FluidVariant getResource()
        {
            FluidContent c = getContent();
            return c.isEmpty() ? FluidVariant.blank() : FluidVariant.of(c.fluid());
        }
        @Override public long getAmount() { return getContent().amount(); }
        @Override public long getCapacity() { return TankFluidHandler.CAPACITY; }
    }



    private CapabilitySetup() { }
}
