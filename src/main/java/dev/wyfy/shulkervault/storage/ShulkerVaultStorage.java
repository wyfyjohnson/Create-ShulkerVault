package dev.wyfy.shulkervault.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class ShulkerVaultStorage extends ItemStackHandler {

    public static final int SLOT_COUNT = 27;
    public static final int DEFAULT_STACK_MULTIPLIER = 4;

    private final IntSupplier multiplierSupplier;
    private final Runnable changeListener;

    public ShulkerVaultStorage(IntSupplier multiplierSupplier, Runnable changeListener) {
        super(SLOT_COUNT);
        this.multiplierSupplier = multiplierSupplier;
        this.changeListener = changeListener;
    }

    public int getStackMultiplier() {
        int value = multiplierSupplier.getAsInt();
        return value < 1 ? 1 : value;
    }

    // Slot helper for inserting items into the slot
    public int getEffectiveStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize() * getStackMultiplier());
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64 * getStackMultiplier();
    }


    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize() * getStackMultiplier());
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        changeListener.run();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        // Save real counts aside so we can restore them after super.serializeNBT runs.
        int[] realCounts = new int[getSlots()];
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack s = getStackInSlot(slot);
            realCounts[slot] = s.getCount();
            if (!s.isEmpty() && s.getCount() > 1) {
                s.setCount(1);
            }
        }

        // Parent serializes everything correctly now because every stack is count=1,
        // which satisfies the vanilla codec's [1;99] range check.
        CompoundTag tag = super.serializeNBT(provider);

        // Restore in-memory stacks to their real counts, and write them to NBT.
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack s = getStackInSlot(slot);
            if (!s.isEmpty()) {
                s.setCount(realCounts[slot]);
            }
        }
        tag.putIntArray("RealCounts", realCounts);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);

        if (nbt.contains("RealCounts", Tag.TAG_INT_ARRAY)) {
            int[] realCounts = nbt.getIntArray("RealCounts");
            int limit = Math.min(realCounts.length, getSlots());
            for (int slot = 0; slot < limit; slot++) {
                ItemStack s = getStackInSlot(slot);
                if (!s.isEmpty() && realCounts[slot] > 0) {
                    s.setCount(realCounts[slot]);
                }
            }
        }
    }
}