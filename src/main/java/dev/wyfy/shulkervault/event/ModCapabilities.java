package dev.wyfy.shulkervault.event;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.entity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = ShulkerVault.MOD_ID)
public class ModCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.SHULKER_VAULT_BE.get(),
                (blockEntity, side) -> blockEntity.getStorage()
        );
    }
}
