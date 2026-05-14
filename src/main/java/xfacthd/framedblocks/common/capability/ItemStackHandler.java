package xfacthd.framedblocks.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.items.ItemStackHandler}.
 * A simple fixed-size slot inventory backed by a list.
 */
public class ItemStackHandler implements IItemHandlerModifiable
{
    protected List<ItemStack> stacks;

    public ItemStackHandler(int slots)
    {
        this.stacks = new ArrayList<>(slots);
        for (int i = 0; i < slots; i++)
        {
            this.stacks.add(ItemStack.EMPTY);
        }
    }

    @Override
    public int getSlots()
    {
        return stacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        validateSlot(slot);
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        validateSlot(slot);
        stacks.set(slot, stack == null ? ItemStack.EMPTY : stack);
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        validateSlot(slot);
        if (!isItemValid(slot, stack)) return stack;

        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

        if (!existing.isEmpty())
        {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) return stack;
            limit -= existing.getCount();
        }

        if (limit <= 0) return stack;

        int toInsert = Math.min(limit, stack.getCount());
        if (!simulate)
        {
            if (existing.isEmpty())
            {
                stacks.set(slot, stack.copyWithCount(toInsert));
            }
            else
            {
                existing.grow(toInsert);
            }
            onContentsChanged(slot);
        }

        return toInsert == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - toInsert);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlot(slot);

        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(toExtract);

        if (!simulate)
        {
            if (toExtract == existing.getCount())
            {
                stacks.set(slot, ItemStack.EMPTY);
            }
            else
            {
                existing.shrink(toExtract);
            }
            onContentsChanged(slot);
        }

        return result;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return true;
    }

    protected void onContentsChanged(int slot) { }

    protected void validateSlot(int slot)
    {
        if (slot < 0 || slot >= stacks.size())
        {
            throw new IllegalArgumentException("Slot " + slot + " is out of range [0, " + stacks.size() + ")");
        }
    }

    // ---- NBT serialization ----

    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        ListTag list = new ListTag();
        for (int i = 0; i < stacks.size(); i++)
        {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty())
            {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                list.add(stack.save(provider, entry));
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", list);
        tag.putInt("Size", stacks.size());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
    {
        int size = tag.contains("Size", Tag.TAG_INT) ? tag.getInt("Size") : stacks.size();
        stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) stacks.add(ItemStack.EMPTY);

        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 0xFF;
            if (slot < stacks.size())
            {
                stacks.set(slot, ItemStack.parseOptional(provider, entry));
            }
        }
    }
}
