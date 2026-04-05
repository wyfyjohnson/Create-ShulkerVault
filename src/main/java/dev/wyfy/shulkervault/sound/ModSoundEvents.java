package dev.wyfy.shulkervault.sound;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, ShulkerVault.MOD_ID);

    // Open sound - reuse shulker open
    public static final DeferredHolder<SoundEvent, SoundEvent> VAULT_OPEN =
            SOUND_EVENTS.register("shulker_vault_open", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "shulker_vault_open")));

    // Open sound - reuse shulker open
    public static final DeferredHolder<SoundEvent, SoundEvent> VAULT_CLOSE =
            SOUND_EVENTS.register("shulker_vault_close", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "shulker_vault_close")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
