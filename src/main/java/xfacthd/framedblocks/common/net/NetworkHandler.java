package xfacthd.framedblocks.common.net;

import net.fabricmc.fabric.api.networking.v1.*;
import xfacthd.framedblocks.common.data.cullupdate.ClientCullingUpdateTracker;
import xfacthd.framedblocks.common.net.payload.*;

/**
 * Fabric replacement for NeoForge's {@code RegisterPayloadHandlersEvent} registration.
 * Called from {@link xfacthd.framedblocks.FramedBlocks#onInitialize()}.
 *
 * Payload codecs are still registered here via {@link PayloadTypeRegistry} so both
 * sides know about all packet types. Handler registration is split:
 *   - Server-side receivers: registered here (common init)
 *   - Client-side receivers: registered in {@link ClientNetworkHandler} (client init)
 */
public final class NetworkHandler
{
    public static void register()
    {
        // Register payload types in the play phase (both directions)
        PayloadTypeRegistry.playC2S().register(
                ServerboundSignUpdatePayload.TYPE,
                ServerboundSignUpdatePayload.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                ServerboundSelectFramingSawRecipePayload.TYPE,
                ServerboundSelectFramingSawRecipePayload.CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                ServerboundEncodeFramingSawPatternPayload.TYPE,
                ServerboundEncodeFramingSawPatternPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ClientboundOpenSignScreenPayload.TYPE,
                ClientboundOpenSignScreenPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ClientboundCullingUpdatePayload.TYPE,
                ClientboundCullingUpdatePayload.CODEC
        );

        // Server-side receivers (C2S)
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundSignUpdatePayload.TYPE,
                (payload, ctx) -> payload.handle(ctx)
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundSelectFramingSawRecipePayload.TYPE,
                (payload, ctx) -> payload.handle(ctx)
        );
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundEncodeFramingSawPatternPayload.TYPE,
                (payload, ctx) -> payload.handle(ctx)
        );

        // Client-side receivers (S2C) are registered in ClientNetworkHandler
        // because they reference client-only classes
    }



    private NetworkHandler() { }
}
