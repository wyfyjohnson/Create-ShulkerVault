package dev.wyfy.shulkervault.event;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = ShulkerVault.MOD_ID, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getCount() > 64) {
            event.getToolTip().add(Component.literal("Quantity: " + event.getItemStack().getCount())
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
