package dev.wyfy.shulkervault.datagen;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ShulkerVault.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        // Telling Datagen where to find the custom Blockbench models
        ModelFile baseVaultModel = new ModelFile.ExistingModelFile(modLoc("block/shulker_vault"), models().existingFileHelper);
        ModelFile advancedVaultModel = new ModelFile.ExistingModelFile(modLoc("block/advanced_shulker_vault"), models().existingFileHelper);

        // Generate the rotational blockstates pointing to those existing models
        directionalBlock(ModBlocks.SHULKER_VAULT.get(), baseVaultModel);
        directionalBlock(ModBlocks.ADVANCED_SHULKER_VAULT.get(), advancedVaultModel);

        // Generate the item models pointing to those existing block models
        simpleBlockItem(ModBlocks.SHULKER_VAULT.get(), baseVaultModel);
        simpleBlockItem(ModBlocks.ADVANCED_SHULKER_VAULT.get(), advancedVaultModel);
    }

}
