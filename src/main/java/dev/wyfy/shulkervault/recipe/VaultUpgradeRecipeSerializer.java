package dev.wyfy.shulkervault.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class VaultUpgradeRecipeSerializer implements RecipeSerializer<VaultUpgradeRecipe> {

    public static final MapCodec<VaultUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    // We now use the getters: VaultUpgradeRecipe::getPattern, etc.
                    ShapedRecipePattern.MAP_CODEC.forGetter(VaultUpgradeRecipe::getPattern),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(VaultUpgradeRecipe::getResult),
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(VaultUpgradeRecipe::isShowNotification)
            ).apply(instance, VaultUpgradeRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultUpgradeRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ShapedRecipePattern.STREAM_CODEC, VaultUpgradeRecipe::getPattern,
                    ItemStack.STREAM_CODEC, VaultUpgradeRecipe::getResult,
                    ByteBufCodecs.BOOL, VaultUpgradeRecipe::isShowNotification,
                    VaultUpgradeRecipe::new
            );

    @Override
    public MapCodec<VaultUpgradeRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, VaultUpgradeRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}