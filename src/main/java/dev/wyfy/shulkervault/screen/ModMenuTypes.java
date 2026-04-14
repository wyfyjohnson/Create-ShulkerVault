package dev.wyfy.shulkervault.screen;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.screen.custom.AdvancedShulkerVaultMenu;
import dev.wyfy.shulkervault.screen.custom.ShulkerVaultMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ShulkerVault.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ShulkerVaultMenu>> SHULKER_VAULT_MENU =
            MENUS.register("shulker_vault_menu", () -> IMenuTypeExtension.create(ShulkerVaultMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedShulkerVaultMenu>> ADVANCED_SHULKER_VAULT_MENU =
            MENUS.register("advanced_shulker_vault_menu", () -> IMenuTypeExtension.create(AdvancedShulkerVaultMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
