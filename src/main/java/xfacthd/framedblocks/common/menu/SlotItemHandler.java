package xfacthd.framedblocks.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xfacthd.framedblocks.common.capability.IItemHandler;
import xfacthd.framedblocks.common.capability.IItemHandlerModifiable;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.items.SlotItemHandler}.
 * Bridges our internal {@link IItemHandler} with vanilla's {@link Slot}.
 */
public class SlotItemHandler extends Slot
{
    private static final Container DUMMY = new SimpleContainer(0);

    private final IItemHandler handler;
    private final int handlerSlot;

    public SlotItemHandler(IItemHandler handler, int slot, int x, int y)
    {
        super(DUMMY, slot, x, y);
        this.handler = handler;
        this.handlerSlot = slot;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return handler.isItemValid(handlerSlot, stack);
    }

    @Override
    public ItemStack getItem()
    {
        return handler.getStackInSlot(handlerSlot);
    }

    @Override
    public void set(ItemStack stack)
    {
        if (handler instanceof IItemHandlerModifiable modifiable)
        {
            modifiable.setStackInSlot(handlerSlot, stack);
        }
        setChanged();
    }

    @Override
    public void setChanged()
    {
        // no-op; the handler manages its own change notification
    }

    @Override
    public int getMaxStackSize()
    {
        return handler.getSlotLimit(handlerSlot);
    }

    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public ItemStack remove(int amount)
    {
        return handler.extractItem(handlerSlot, amount, false);
    }

    @Override
    public boolean hasItem()
    {
        return !getItem().isEmpty();
    }

    public IItemHandler getItemHandler()
    {
        return handler;
    }
}
