package xfacthd.framedblocks.client.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import xfacthd.framedblocks.common.data.cullupdate.ClientCullingUpdateTracker;
import xfacthd.framedblocks.common.net.payload.*;

/**
 * Registers client-side (S2C) packet receivers.
 * Called from {@link xfacthd.framedblocks.client.FBClient#onInitializeClient()}.
 */
@Environment(EnvType.CLIENT)
public final class ClientNetworkHandler
{
    public static void register()
    {
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundOpenSignScreenPayload.TYPE,
                (payload, ctx) -> payload.handle(ctx)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundCullingUpdatePayload.TYPE,
                ClientCullingUpdateTracker::handleCullingUpdates
        );
    }



    private ClientNetworkHandler() { }
}
