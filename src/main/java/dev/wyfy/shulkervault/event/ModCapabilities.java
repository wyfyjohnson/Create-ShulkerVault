package dev.wyfy.shulkervault.event;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.entity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

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

        // Advanced vault - exposes combined handler (main storage + package slot)
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADVANCED_SHULKER_VAULT_BE.get(),
                (blockEntity, side) -> new CombinedInvWrapper(
                        blockEntity.getStorage(),
                        blockEntity.getPackageSlot()
                )
        );
    }
}
