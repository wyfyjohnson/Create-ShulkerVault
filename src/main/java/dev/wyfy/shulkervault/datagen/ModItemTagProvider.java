package dev.wyfy.shulkervault.datagen;

import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.ModBlocks;
import dev.wyfy.shulkervault.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {

    public ModItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, blockTags, ShulkerVault.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        TagKey<Item> shulkerBoxTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("minecraft", "shulker_boxes"));
        this.tag(shulkerBoxTag)
                .add(ModBlocks.SHULKER_VAULT.asItem())
                .add(ModBlocks.ADVANCED_SHULKER_VAULT.asItem());

        // 2. The Common (c) Shulker Shell Tag for Mod Compatibility
        TagKey<Item> shulkerShellsTag = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "shulker_shells"));
        this.tag(shulkerShellsTag)
                .add(ModItems.REINFORCED_SHULKER_SHELL.get());
    }
}