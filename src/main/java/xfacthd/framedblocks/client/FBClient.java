package xfacthd.framedblocks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import xfacthd.framedblocks.client.loader.FramedModelLoadingPlugin;
import xfacthd.framedblocks.client.render.special.*;
import xfacthd.framedblocks.client.render.debug.FramedBlockDebugRenderer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import xfacthd.framedblocks.FramedBlocks;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.render.FramedBlockColor;
import xfacthd.framedblocks.api.util.FramedConstants;
import xfacthd.framedblocks.common.FBContent;
import xfacthd.framedblocks.common.block.IFramedDoubleBlock;
import xfacthd.framedblocks.client.render.color.FramedFlowerPotColor;
import xfacthd.framedblocks.client.render.color.FramedTargetBlockColor;
import xfacthd.framedblocks.client.render.particle.FluidSpriteParticle;
import xfacthd.framedblocks.client.screen.*;
import xfacthd.framedblocks.client.screen.widget.BlockPreviewTooltipComponent;
import xfacthd.framedblocks.client.util.*;
import xfacthd.framedblocks.client.net.ClientNetworkHandler;
import xfacthd.framedblocks.client.render.block.*;

@Environment(EnvType.CLIENT)
public final class FBClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        registerScreens();
        registerColors();
        registerParticleProviders();
        registerBlockEntityRenderers();
        registerClientTickEvents();
        registerClientNetworkEvents();
        registerTooltipComponents();
        registerKeyBindings();
        ClientNetworkHandler.register();

        registerModelLoader();
        registerRenderEvents();
        FramedBlockDebugRenderer.init();

        FramedBlocks.LOGGER.info("FramedBlocks client initialized (Fabric)");
    }

    // ---- Screens ----

    private static void registerScreens()
    {
        MenuScreens.register(FBContent.MENU_TYPE_FRAMED_STORAGE.value(), FramedStorageScreen::new);
        MenuScreens.register(FBContent.MENU_TYPE_FRAMED_DOUBLE_CHEST.value(), FramedStorageScreen::new);
        MenuScreens.register(FBContent.MENU_TYPE_FRAMING_SAW.value(), FramingSawScreen::create);
        MenuScreens.register(FBContent.MENU_TYPE_POWERED_FRAMING_SAW.value(), PoweredFramingSawScreen::new);
    }

    // ---- Colors ----

    private static void registerColors()
    {
        ColorProviderRegistry.BLOCK.register(FramedBlockColor.INSTANCE,
                getFramedBlocks());
        ColorProviderRegistry.BLOCK.register(FramedFlowerPotColor.INSTANCE,
                FBContent.BLOCK_FRAMED_FLOWER_POT.value());
        ColorProviderRegistry.BLOCK.register(FramedTargetBlockColor.INSTANCE,
                FBContent.BLOCK_FRAMED_TARGET.value());

        ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
                FramedBlockColor.INSTANCE.getColor(null, null, null, tintIndex),
                getFramedItems());
        ColorProviderRegistry.ITEM.register((stack, tintIndex) ->
                FramedTargetBlockColor.INSTANCE.getColor(null, null, null, tintIndex),
                FBContent.BLOCK_FRAMED_TARGET.value());
    }

    private static Block[] getFramedBlocks()
    {
        return FBContent.getRegisteredBlocks()
                .stream()
                .map(Holder::value)
                .filter(IFramedBlock.class::isInstance)
                .map(IFramedBlock.class::cast)
                .filter(FBClient::useDefaultColorHandler)
                .toArray(Block[]::new);
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private static ItemLike[] getFramedItems()
    {
        return FBContent.getRegisteredBlocks()
                .stream()
                .map(Holder::value)
                .filter(IFramedBlock.class::isInstance)
                .map(IFramedBlock.class::cast)
                .filter(FBClient::useDefaultColorHandler)
                .toArray(ItemLike[]::new);
    }

    // ---- Particles ----

    private static void registerParticleProviders()
    {
        ParticleFactoryRegistry.getInstance().register(
                FBContent.FLUID_PARTICLE.value(),
                FluidSpriteParticle.Provider::new
        );
    }

    // ---- Block Entity Renderers ----

    private static void registerBlockEntityRenderers()
    {
        BlockEntityRendererRegistry.register(FBContent.BE_TYPE_FRAMED_SIGN.value(), FramedSignRenderer::new);
        BlockEntityRendererRegistry.register(FBContent.BE_TYPE_FRAMED_HANGING_SIGN.value(), FramedHangingSignRenderer::new);
        BlockEntityRendererRegistry.register(FBContent.BE_TYPE_FRAMED_CHEST.value(), FramedChestRenderer::new);
        BlockEntityRendererRegistry.register(FBContent.BE_TYPE_FRAMED_ITEM_FRAME.value(), FramedItemFrameRenderer::new);
        BlockEntityRendererRegistry.register(FBContent.BE_TYPE_FRAMED_TANK.value(), FramedTankRenderer::new);
    }

    // ---- Tick Events ----

    private static void registerClientTickEvents()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            ClientTaskQueue.onClientTick();
            KeyMappings.onClientTick();
        });
    }

    // ---- Network / Lifecycle Events ----

    private static void registerClientNetworkEvents()
    {
        // Recipes updated: update client-side framing saw recipe cache
        // RecipeSyncCallback fires whenever the server sends an UpdateRecipesPacket
        net.fabricmc.fabric.api.client.recipe.v1.RecipeSyncCallback.EVENT.register(
                recipeManager -> ClientEventHandler.onRecipesUpdated(null, recipeManager)
        );

        // On disconnect: clear client recipe cache
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ClientEventHandler.onClientDisconnect()
        );
    }

    // ---- Tooltip Components ----

    private static void registerTooltipComponents()
    {
        TooltipComponentCallback.EVENT.register(data ->
        {
            if (data instanceof BlockPreviewTooltipComponent.Component comp)
            {
                return new BlockPreviewTooltipComponent(comp);
            }
            return null;
        });
    }

    // ---- Key Bindings ----

    private static void registerKeyBindings()
    {
        // KeyMappings are registered via KeyBindingHelper.registerKeyBinding()
        // which is called lazily on first access of the KEYMAPPING_* fields.
        // Force initialization so they're registered before the options screen loads.
        KeyMappings.KEYMAPPING_UPDATE_CULLING.getDefaultKey(); // force static init
        KeyMappings.KEYMAPPING_WIPE_CACHE.getDefaultKey();
    }

    // ---- Helpers ----

    // ---- Model Loading ----

    private static void registerModelLoader()
    {
        ModelLoadingPlugin.register(new FramedModelLoadingPlugin());
    }

    // ---- World Render Events ----

    private static void registerRenderEvents()
    {
        WorldRenderEvents.BLOCK_OUTLINE.register(BlockOutlineRenderer::onRenderBlockHighlight);
        WorldRenderEvents.AFTER_PARTICLES.register(GhostBlockRenderer::onRenderAfterParticles);
    }

    private static boolean useDefaultColorHandler(IFramedBlock block)
    {
        var type = block.getBlockType();
        return type != xfacthd.framedblocks.common.data.BlockType.FRAMED_FLOWER_POT
                && type != xfacthd.framedblocks.common.data.BlockType.FRAMED_TARGET;
    }
}
