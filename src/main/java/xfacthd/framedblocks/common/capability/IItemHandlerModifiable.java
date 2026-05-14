package xfacthd.framedblocks.common.capability;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.items.IItemHandlerModifiable}.
 */
public interface IItemHandlerModifiable extends IItemHandler
{
    void setStackInSlot(int slot, ItemStack stack);
}
