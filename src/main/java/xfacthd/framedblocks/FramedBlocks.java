package xfacthd.framedblocks;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import xfacthd.framedblocks.common.FBContent;
import xfacthd.framedblocks.common.compat.CompatHandler;
import xfacthd.framedblocks.common.crafting.FramingSawRecipeCache;
import xfacthd.framedblocks.common.data.*;
import xfacthd.framedblocks.common.config.*;
import xfacthd.framedblocks.common.data.capabilities.CapabilitySetup;
import xfacthd.framedblocks.common.data.camo.CamoContainerFactories;
import xfacthd.framedblocks.common.data.cullupdate.CullingUpdateTracker;
import xfacthd.framedblocks.common.net.NetworkHandler;
import xfacthd.framedblocks.common.util.EventHandler;

public final class FramedBlocks implements ModInitializer
{
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        // 1. Load configs (must be first — other systems read config values during init)
        ClientConfig.init();
        ServerConfig.init();
        DevToolsConfig.init();

        // 2. Register all blocks, items, block entities, menus, recipes, creative tab, etc.
        FBContent.init();

        // 3. Common setup (mirrors FMLCommonSetupEvent)
        commonSetup();

        // 4. Register capabilities (Fabric Transfer API + Team Reborn Energy)
        CapabilitySetup.register();

        // 5. Register networking (payload types + server-side receivers)
        NetworkHandler.register();

        // 6. Register event hooks
        EventHandler.register();
        CullingUpdateTracker.register();
        FramingSawRecipeCache.registerReloadListener();

        // 4. Data maps stub (NeoForge DataMapType replacement - Step 8)
        FramedDataMaps.init();

        // 9. Server config reload on data pack reload
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
                (server, resourceManager, success) -> { if (success) ServerConfig.onConfigReloaded(); }
        );

        // 10. Server shutdown hook
        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
        {
            EventHandler.onServerShutdown();
            CullingUpdateTracker.onServerShutdown();
        });

        // 6. Compat module initialization
        CompatHandler.init();

        LOGGER.info("FramedBlocks initialized (Fabric)");
    }

    private static void commonSetup()
    {
        // Mirrors NeoForge FMLCommonSetupEvent
        StateCacheBuilder.ensureStateCachesInitialized();
        FramedBlueprintItem.init();
        CamoContainerFactories.registerCamoFactories();

        // Register PoI type extension for framed lightning rod
        // NeoForge: ExtendPoiTypesEvent — Fabric: direct registry manipulation after freeze
        // This is done via tag injection; the block is registered in a data pack tag instead.
        // See src/main/resources/data/minecraft/tags/worldgen/poi_type/lightning_rod.json (to be created)

        // Predicate initialization
        xfacthd.framedblocks.common.data.facepreds.FullFacePredicates.PREDICATES.initialize();
        xfacthd.framedblocks.common.data.skippreds.SideSkipPredicates.PREDICATES.initialize();
        xfacthd.framedblocks.common.data.conpreds.ConnectionPredicates.PREDICATES.initialize();

        CompatHandler.commonSetup();
        LOGGER.debug("FramedBlocks common setup complete");
    }
}
