package xfacthd.framedblocks.common.capability;

/**
 * Internal energy storage interface, mirrors the NeoForge IEnergyStorage contract.
 *
 * Cross-mod energy interop is handled via Team Reborn Energy API
 * ({@code team.reborn.energy.api.EnergyStorage}), which is part of Fabric API in 1.21.1.
 * See {@link CapabilitySetup} for the registration bridge.
 */
public interface IEnergyStorage
{
    /**
     * Add energy to the storage.
     * @return the amount of energy that was accepted
     */
    int receiveEnergy(int maxReceive, boolean simulate);

    /**
     * Remove energy from the storage.
     * @return the amount of energy that was removed
     */
    int extractEnergy(int maxExtract, boolean simulate);

    int getEnergyStored();

    int getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
