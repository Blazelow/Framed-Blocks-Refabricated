package xfacthd.framedblocks.common.capability;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.items.IItemHandler}.
 * Used internally between block entities and menus — not exposed as a cross-mod API.
 * Cross-mod item transfer is handled via the Fabric Transfer API
 * ({@code net.fabricmc.fabric.api.transfer.v1.item.ItemStorage}).
 */
public interface IItemHandler
{
    int getSlots();

    ItemStack getStackInSlot(int slot);

    /**
     * Insert an item into the given slot.
     * @return the remainder that could not be inserted
     */
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    /**
     * Extract an item from the given slot.
     * @return the extracted stack (may be less than {@code amount})
     */
    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
