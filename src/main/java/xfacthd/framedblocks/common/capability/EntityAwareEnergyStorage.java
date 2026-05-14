package xfacthd.framedblocks.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Fabric replacement for NeoForge's {@code EnergyStorage}-extending entity-aware energy handler.
 * Implements our internal {@link IEnergyStorage} interface.
 * Exposed cross-mod via Team Reborn Energy API in {@link CapabilitySetup}.
 */
public class EntityAwareEnergyStorage implements IEnergyStorage
{
    protected int energy;
    protected final int capacity;
    protected final int maxReceive;
    protected final int maxExtract;
    private final Runnable changeNotifier;

    public EntityAwareEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable changeNotifier)
    {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.changeNotifier = changeNotifier;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate)
    {
        int received = Math.min(toReceive, Math.min(maxReceive, capacity - energy));
        if (received > 0 && !simulate)
        {
            energy += received;
            changeNotifier.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate)
    {
        int extracted = Math.min(toExtract, Math.min(maxExtract, energy));
        if (extracted > 0 && !simulate)
        {
            energy -= extracted;
            changeNotifier.run();
        }
        return extracted;
    }

    /** Internal-only extraction that bypasses the maxExtract limit. */
    public void extractEnergyInternal(int amount)
    {
        energy = Math.max(0, energy - amount);
        changeNotifier.run();
    }

    @Override
    public int getEnergyStored() { return energy; }

    @Override
    public int getMaxEnergyStored() { return capacity; }

    @Override
    public boolean canReceive() { return maxReceive > 0; }

    @Override
    public boolean canExtract() { return maxExtract > 0; }

    public int getMaxReceive() { return maxReceive; }

    public int getCapacity() { return capacity; }

    // ---- NBT ----

    public Tag serializeNBT(HolderLookup.Provider provider)
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("energy", energy);
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, Tag tag)
    {
        energy = tag instanceof CompoundTag cmpTag ? cmpTag.getInt("energy") : 0;
        energy = Math.min(energy, capacity);
    }
}
