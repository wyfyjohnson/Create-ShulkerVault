package dev.wyfy.shulkervault.screen.custom;

import dev.wyfy.shulkervault.storage.ShulkerVaultStorage;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OversizedSlotItemHandler extends SlotItemHandler {
    public OversizedSlotItemHandler(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);

    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        IItemHandler h = getItemHandler();
        if (h instanceof ShulkerVaultStorage svs) {
            return svs.getEffectiveStackLimit(getSlotIndex(),stack);
        }
        return Math.min(getItemHandler().getSlotLimit(getSlotIndex()), stack.getMaxStackSize());
    }
}
