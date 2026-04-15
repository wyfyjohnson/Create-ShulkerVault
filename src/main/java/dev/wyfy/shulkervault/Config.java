package dev.wyfy.shulkervault;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ShulkerVault.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue STACK_MULTIPLIER = BUILDER
            .comment("Per-slot stack multiplier for Shulker Vaults. A value of 4 means a slot that normally holds 64 items will hold 256.")
            .defineInRange("stackMultiplier", 4, 1, 64);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int stackMultiplier;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        stackMultiplier = STACK_MULTIPLIER.get();
    }
}