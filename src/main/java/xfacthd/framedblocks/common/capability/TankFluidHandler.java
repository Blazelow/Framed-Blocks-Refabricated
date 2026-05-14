package xfacthd.framedblocks.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import xfacthd.framedblocks.common.blockentity.special.FramedTankBlockEntity;
import xfacthd.framedblocks.common.data.component.FluidContent;

/**
 * Fabric fluid handler for {@link FramedTankBlockEntity}.
 * Stores fluid internally using {@link FluidContent} (our own FluidStack replacement).
 *
 * Cross-mod fluid transfer is exposed via the Fabric Transfer API in {@link CapabilitySetup}
 * using {@code net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED}.
 *
 * NeoForge IFluidHandler / FluidStack removed. Internal fluid representation uses
 * our {@link FluidContent} record (Fluid + amount in milli-buckets).
 */
public final class TankFluidHandler
{
    public static final long CAPACITY = 16 * FluidContent.BUCKET_VOLUME; // 16,000 mB
    public static final String FLUID_NBT_KEY = "fluid";

    private final FramedTankBlockEntity blockEntity;
    private Fluid fluid = Fluids.EMPTY;
    private long amount = 0;

    public TankFluidHandler(FramedTankBlockEntity blockEntity)
    {
        this.blockEntity = blockEntity;
    }

    // ---- Query ----

    public boolean isEmpty() { return fluid == Fluids.EMPTY || amount <= 0; }

    public Fluid getFluid() { return fluid; }

    public long getAmount() { return amount; }

    public long getCapacity() { return CAPACITY; }

    public FluidContent getFluidContent()
    {
        return isEmpty() ? FluidContent.EMPTY : new FluidContent(fluid, amount);
    }

    // ---- Mutation (used by Fabric Transfer API bridge and block interactions) ----

    /**
     * Try to fill this tank with the given fluid type and amount.
     * @return how much was actually accepted
     */
    public long fill(Fluid incoming, long incomingAmount, boolean simulate)
    {
        if (incoming == Fluids.EMPTY || incomingAmount <= 0) return 0;
        if (!isEmpty() && fluid != incoming) return 0; // already has a different fluid

        long space = CAPACITY - amount;
        long toFill = Math.min(space, incomingAmount);
        if (toFill <= 0) return 0;

        if (!simulate)
        {
            if (isEmpty()) fluid = incoming;
            amount += toFill;
            onContentsChanged();
        }
        return toFill;
    }

    /**
     * Try to drain fluid.
     * @return the amount actually drained
     */
    public long drain(long maxDrain, boolean simulate)
    {
        if (isEmpty() || maxDrain <= 0) return 0;
        long toDrain = Math.min(amount, maxDrain);
        if (!simulate)
        {
            amount -= toDrain;
            if (amount == 0) fluid = Fluids.EMPTY;
            onContentsChanged();
        }
        return toDrain;
    }

    /** Drain a specific fluid type; returns 0 if the stored fluid doesn't match. */
    public long drain(Fluid requested, long maxDrain, boolean simulate)
    {
        if (fluid != requested) return 0;
        return drain(maxDrain, simulate);
    }

    public void setFluidContent(FluidContent content)
    {
        this.fluid = content.fluid();
        this.amount = content.amount();
        onContentsChanged();
    }

    private void onContentsChanged()
    {
        blockEntity.onTankContentsChanged();
    }

    // ---- NBT ----

    public void load(CompoundTag tag, HolderLookup.Provider provider)
    {
        FluidContent content = FluidContent.CODEC.parse(
                net.minecraft.nbt.NbtOps.INSTANCE, tag.get(FLUID_NBT_KEY)
        ).result().orElse(FluidContent.EMPTY);
        fluid = content.fluid();
        amount = content.amount();
    }

    public void save(CompoundTag nbt, HolderLookup.Provider provider)
    {
        if (!isEmpty())
        {
            FluidContent.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, getFluidContent())
                    .result()
                    .ifPresent(tag -> nbt.put(FLUID_NBT_KEY, tag));
        }
    }
}
