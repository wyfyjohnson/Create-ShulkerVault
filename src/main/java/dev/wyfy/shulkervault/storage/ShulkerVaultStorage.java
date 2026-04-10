package dev.wyfy.shulkervault.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ShulkerVaultStorage extends ItemStackHandler {

    private static final String TAG_STACK_MULTIPLIER = "StackMultiplier";
    public static final int DEFAULT_STACK_MULTIPLIER= 4;

    private final int stackMultiplier;

    public ShulkerVaultStorage(int size, int stackMultiplier) {
        super(size);
        this.stackMultiplier = stackMultiplier;
    }

    public int getStackMultiplier() {
        return stackMultiplier;
    }

    /**
     * The per-item insert limit for a given slot.
     *
     * Clamps to both the slot limit and the item's native max stack size,
     * each scaled by the multiplier, so a 64-stack item can go up to
     * 64 * multiplier, a 16-stack item up to 16 * multiplier, and an
     * unstackable item up to 1 * multiplier.
     */
    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot) * stackMultiplier, stack.getMaxStackSize() * stackMultiplier);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putInt(TAG_STACK_MULTIPLIER, stackMultiplier);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
    }

    /*
     * Example usage in your block entity's load():
     *   int multiplier = ShulkerVaultStorage.readMultiplierFromNBT(savedTag, 4);
     *   this.storage = new ShulkerVaultStorage(27, multiplier);
     *   this.storage.deserializeNBT(provider, savedTag);
     */
    public static int readMultiplierFromNBT(CompoundTag nbt, int defaultMultiplier) {
        if (nbt.contains(TAG_STACK_MULTIPLIER)) {
            return nbt.getInt(TAG_STACK_MULTIPLIER);
        }
        return defaultMultiplier;
    }
}