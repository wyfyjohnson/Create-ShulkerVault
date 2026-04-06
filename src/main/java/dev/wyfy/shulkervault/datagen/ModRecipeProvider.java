package dev.wyfy.shulkervault.datagen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeBuilder;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.ModBlocks;
import dev.wyfy.shulkervault.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        // 1. Sequenced Assembly: Reinforced Shulker Shell
        // 2 loops: Deploy Iron Sheet -> Deploy Iron Sheet -> Press
        new SequencedAssemblyRecipeBuilder(ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "reinforced_shulker_shell"))
                .require(Items.SHULKER_SHELL)
                .transitionTo(ModItems.REINFORCED_SHULKER_SHELL.get())
                .addOutput(ModItems.REINFORCED_SHULKER_SHELL.get(), 1.0f)
                .loops(2)
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllItems.IRON_SHEET.get()))
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllItems.IRON_SHEET.get()))
                .addStep(PressingRecipe::new, rb -> rb)
                .build(recipeOutput);

        // 2. Shaped Recipe: Base Shulker Vault
        // 3 Create Item Vaults sandwiched between 2 Reinforced Shells
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SHULKER_VAULT.get())
                .pattern(" R ")
                .pattern("VVV")
                .pattern(" R ")
                .define('R', ModItems.REINFORCED_SHULKER_SHELL.get())
                .define('V', AllBlocks.ITEM_VAULT.get())
                .unlockedBy("has_item_vault", has(AllBlocks.ITEM_VAULT.get()))
                .save(recipeOutput);

        // 3. Mechanical Crafting: Advanced Shulker Vault
        // Vertical assembly: Electron Tube -> Packager -> Shulker Vault
        MechanicalCraftingRecipeBuilder.shapedRecipe(ModBlocks.ADVANCED_SHULKER_VAULT.get())
                .patternLine("T")
                .patternLine("P")
                .patternLine("V")
                .key('T', AllItems.ELECTRON_TUBE.get())
                .key('P', AllBlocks.PACKAGER.get())
                .key('V', ModBlocks.SHULKER_VAULT.get())
                .build(recipeOutput);
    }
}