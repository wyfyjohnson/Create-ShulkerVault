package dev.wyfy.shulkervault.recipe;

import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import dev.wyfy.shulkervault.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class VaultUpgradeRecipe extends MechanicalCraftingRecipe {

    // Store our own references since the superclass hides them in 1.21
    private final ShapedRecipePattern pattern;
    private final ItemStack result;
    private final boolean showNotification;

    public VaultUpgradeRecipe(ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        // 1.21 Requirement: We must pass a Group (empty) and Category (MISC) to Create
        super("", CraftingBookCategory.MISC, pattern, result, showNotification);
        this.pattern = pattern;
        this.result = result;
        this.showNotification = showNotification;
    }

    // Getters for the Serializer
    public ShapedRecipePattern getPattern() { return this.pattern; }
    public ItemStack getResult() { return this.result; }
    public boolean isShowNotification() { return this.showNotification; }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);

        for (int i = 0; i < input.size(); i++) {
            ItemStack ingredient = input.getItem(i);
            if (ingredient.is(ModBlocks.SHULKER_VAULT.get().asItem())) {
                CustomData blockData = ingredient.get(DataComponents.BLOCK_ENTITY_DATA);
                if (blockData != null) {
                    result.set(DataComponents.BLOCK_ENTITY_DATA, blockData);
                }
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.VAULT_UPGRADE.get();
    }
}