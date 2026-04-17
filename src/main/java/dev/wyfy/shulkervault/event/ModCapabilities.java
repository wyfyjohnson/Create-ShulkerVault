package dev.wyfy.shulkervault.event;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.entity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = ShulkerVault.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Base vault - exposes main storage
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SHULKER_VAULT_BE.get(),
                (blockEntity, side) -> blockEntity.getStorage()
        );

        // Advanced vault - exposes cached package-aware handler
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADVANCED_SHULKER_VAULT_BE.get(),
                (blockEntity, side) -> blockEntity.getItemHandler()
        );
    }
}
