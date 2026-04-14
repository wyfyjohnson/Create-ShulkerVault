package dev.wyfy.shulkervault.recipe;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ShulkerVault.MOD_ID);

    public static final Supplier<VaultUpgradeRecipeSerializer> VAULT_UPGRADE =
            SERIALIZERS.register("vault_upgrade", VaultUpgradeRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
