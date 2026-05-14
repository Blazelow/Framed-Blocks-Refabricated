package xfacthd.framedblocks.common.capability;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.items.ItemHandlerHelper}.
 */
public final class ItemHandlerHelper
{
    /**
     * Insert a stack into the first available slot of the handler.
     * @return the remainder that could not be inserted
     */
    public static ItemStack insertItemStacked(IItemHandler handler, ItemStack stack, boolean simulate)
    {
        if (handler == null || stack.isEmpty()) return stack;

        // Try inserting into slots that already have the same item first
        for (int i = 0; i < handler.getSlots(); i++)
        {
            ItemStack existing = handler.getStackInSlot(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(stack, existing))
            {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        // Then try empty slots
        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (handler.getStackInSlot(i).isEmpty())
            {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /**
     * Insert a single stack into the specified handler slot.
     * @return the remainder
     */
    public static ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate)
    {
        if (handler == null || stack.isEmpty()) return stack;
        for (int i = 0; i < handler.getSlots(); i++)
        {
            stack = handler.insertItem(i, stack, simulate);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }



    private ItemHandlerHelper() { }
}
