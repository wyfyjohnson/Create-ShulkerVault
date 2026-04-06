package dev.wyfy.shulkervault.datagen;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ShulkerVault.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.REINFORCED_SHULKER_SHELL.get());
    }
}
