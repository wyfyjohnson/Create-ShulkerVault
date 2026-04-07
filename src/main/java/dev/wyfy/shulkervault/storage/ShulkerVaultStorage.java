package dev.wyfy.shulkervault.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ShulkerVaultStorage extends ItemStackHandler {

    public static final int SLOT_COUNT = 27;
    public static final int DEFAULT_STACK_MULTIPLIER = 4;

    private final int stackMultiplier;
    private final Runnable changeListener;

    public ShulkerVaultStorage() {
        this(DEFAULT_STACK_MULTIPLIER, () -> {});
    }

    public ShulkerVaultStorage(int stackMultiplier, Runnable changeListener) {
        super(SLOT_COUNT);
        if (stackMultiplier < 1) {
            throw new IllegalArgumentException("stackMultiplier must be >= 1, got " + stackMultiplier);
        }
        this.stackMultiplier = stackMultiplier;
        this.changeListener = changeListener;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE * stackMultiplier;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize() * stackMultiplier);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        changeListener.run();
    }

    public int getStackMultiplier() {
        return stackMultiplier;
    }
}