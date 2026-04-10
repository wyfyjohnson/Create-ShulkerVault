package dev.wyfy.shulkervault.storage;

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
}