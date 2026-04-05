package dev.wyfy.shulkervault.item;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShulkerVault.MOD_ID);

    public static final DeferredItem<Item> REINFORCED_SHULKER_SHELL = ITEMS.register("reinforced_shulker_shell",
            () -> new Item(new Item.Properties()));
//    public static final DeferredItem<Item> SHULKER_VAULT = ITEMS.re

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
