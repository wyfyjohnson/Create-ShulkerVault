package dev.wyfy.shulkervault.network;

import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public record VaultInventoryActionPayload(
        VaultAction action,
        int containerId,
        int slotId
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VaultInventoryActionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "vault_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultInventoryActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    NeoForgeStreamCodecs.enumCodec(VaultAction.class), VaultInventoryActionPayload::action,
                    ByteBufCodecs.INT, VaultInventoryActionPayload::containerId,
                    ByteBufCodecs.INT, VaultInventoryActionPayload::slotId,
                    VaultInventoryActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
