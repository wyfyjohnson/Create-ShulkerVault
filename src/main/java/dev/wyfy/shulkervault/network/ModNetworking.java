package dev.wyfy.shulkervault.network;

import com.mojang.logging.LogUtils;
import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.screen.custom.ShulkerVaultMenu;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@EventBusSubscriber(modid = ShulkerVault.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                VaultInventoryActionPayload.TYPE,
                VaultInventoryActionPayload.STREAM_CODEC,
                ModNetworking::handleVaultAction
        );
    }

    private static void handleVaultAction(VaultInventoryActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }

            // Verify player has the correct menu open with matching container ID
            if (!(player.containerMenu instanceof ShulkerVaultMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }

            // Route to appropriate handler based on action
            if (payload.action() == VaultAction.CTRL_SHIFT_CLICK) {
                menu.handleCtrlShiftClick(payload.slotId());
            }
            // Future actions (PACK_PACKAGE, UNPACK_PACKAGE) can be added here
        });
    }
}
