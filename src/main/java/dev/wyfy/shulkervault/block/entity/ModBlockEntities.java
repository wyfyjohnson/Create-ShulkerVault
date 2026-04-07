package dev.wyfy.shulkervault.block.entity;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShulkerVault.MOD_ID);

    public static final Supplier<BlockEntityType<ShulkerVaultBlockEntity>> SHULKER_VAULT_BE =
            BLOCK_ENTITIES.register("shulker_vault_be", () -> BlockEntityType.Builder.of(
                    ShulkerVaultBlockEntity::new,
                    ModBlocks.SHULKER_VAULT.get(),
                    ModBlocks.ADVANCED_SHULKER_VAULT.get()
            ).build(null));

    public  static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}