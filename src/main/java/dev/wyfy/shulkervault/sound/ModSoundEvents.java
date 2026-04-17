package dev.wyfy.shulkervault.sound;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, ShulkerVault.MOD_ID);

    // Open sound - reuse shulker box open
    public static final DeferredHolder<SoundEvent, SoundEvent> VAULT_OPEN =
            SOUND_EVENTS.register("shulker_vault_open", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "shulker_vault_open")));

    // Open sound - reuse shulker box close
    public static final DeferredHolder<SoundEvent, SoundEvent> VAULT_CLOSE =
            SOUND_EVENTS.register("shulker_vault_close", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "shulker_vault_close")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    // ==================== Package Sound Helpers ====================

    /**
     * Plays the package unpack sound effect (matching Create's PACKAGE_POP).
     * Called when a package is successfully unpacked into main storage.
     */
    public static void playPackageUnpack(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) return;

        // CHISELED_BOOKSHELF_BREAK (primary)
        level.playSound(null, pos,
                SoundEvents.CHISELED_BOOKSHELF_BREAK,
                SoundSource.BLOCKS,
                0.75f, 1.0f);

        // WOOL_BREAK (secondary, layered)
        level.playSound(null, pos,
                SoundEvents.WOOL_BREAK,
                SoundSource.BLOCKS,
                0.25f, 1.15f);
    }

    /**
     * Plays the package routed sound effect (matching Create's DEPOT_PLOP).
     * Called when a package is stored in the package slot (unpack failed).
     */
    public static void playPackageRouted(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) return;

        level.playSound(null, pos,
                SoundEvents.ITEM_FRAME_ADD_ITEM,
                SoundSource.BLOCKS,
                0.25f, 1.25f);
    }
}
