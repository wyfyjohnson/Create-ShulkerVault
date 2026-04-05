package dev.wyfy.shulkervault.item;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShulkerVault.MOD_ID);

    public static final Supplier<CreativeModeTab> SHULKER_VAULT_ITEMS_TAB = CREATIVE_MODE_TAB.register("shulker_vault_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.REINFORCED_SHULKER_SHELL.get()))
                    .title(Component.translatable("creativetab.shulkervault.shulker_vault_items"))
                    .displayItems(itemDisplayParameters, output) -> )
                    .build());

    public static void register(IEventBus eventBus) {
       CREATIVE_MODE_TAB.register(eventBus);
    }
}
