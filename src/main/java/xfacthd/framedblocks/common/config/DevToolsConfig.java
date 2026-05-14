package xfacthd.framedblocks.common.config;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.common.config.impl.FabricConfigSpec;
import xfacthd.framedblocks.api.util.ConfigView;
import xfacthd.framedblocks.api.util.Utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class DevToolsConfig
{
    public static final ExtConfigView.DevTools VIEW = (ExtConfigView.DevTools) ConfigView.DevTools.INSTANCE;
    private static FabricConfigSpec SPEC;

    private static final String KEY_DOUBLE_BLOCK_PART_DEBUG = "doubleBlockPartDebug";
    private static final String KEY_CONNECTION_DEBUG = "connectionDebug";
    private static final String KEY_QUAD_WINDING_DEBUG = "quadWindingDebug";
    private static final String KEY_STATE_MERGER_DEBUG = "stateMergerDebug";
    private static final String KEY_STATE_MERGER_DEBUG_FILTER = "stateMergerDebugFilter";
    private static final String KEY_OCCLUSION_SHAPE_DEBUG = "occlusionShapeDebug";

    public static final String TRANSLATION_DOUBLE_BLOCK_PART_DEBUG = translate(KEY_DOUBLE_BLOCK_PART_DEBUG);
    public static final String TRANSLATION_CONNECTION_DEBUG = translate(KEY_CONNECTION_DEBUG);
    public static final String TRANSLATION_QUAD_WINDING_DEBUG = translate(KEY_QUAD_WINDING_DEBUG);
    public static final String TRANSLATION_STATE_MERGER_DEBUG = translate(KEY_STATE_MERGER_DEBUG);
    public static final String TRANSLATION_STATE_MERGER_DEBUG_FILTER = translate(KEY_STATE_MERGER_DEBUG_FILTER);
    public static final String TRANSLATION_OCCLUSION_SHAPE_DEBUG = translate(KEY_OCCLUSION_SHAPE_DEBUG);

    private static final FabricConfigSpec.BooleanValue DOUBLE_BLOCK_PART_DEBUG_VALUE;
    private static final FabricConfigSpec.BooleanValue CONNECTION_DEBUG_VALUE;
    private static final FabricConfigSpec.BooleanValue QUAD_WINDING_DEBUG_VALUE;
    private static final FabricConfigSpec.BooleanValue STATE_MERGER_DEBUG_VALUE;
    private static final FabricConfigSpec.StringValue STATE_MERGER_DEBUG_FILTER_VALUE;
    private static final FabricConfigSpec.BooleanValue OCCLUSION_SHAPE_DEBUG_VALUE;

    private static boolean doubleBlockPartDebug = false;
    private static boolean connectionDebug = false;
    private static boolean quadWindingDebug = false;
    private static boolean stateMergerDebug = false;
    private static Pattern stateMergerDebugFilter = null;
    private static boolean occlusionShapeDebug = false;

    public static void init()
    {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && SPEC != null)
        {
            SPEC.load();
            onConfigReloaded();
        }
    }

    static
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            SPEC = null;
            DOUBLE_BLOCK_PART_DEBUG_VALUE = null;
            CONNECTION_DEBUG_VALUE = null;
            QUAD_WINDING_DEBUG_VALUE = null;
            STATE_MERGER_DEBUG_VALUE = null;
            STATE_MERGER_DEBUG_FILTER_VALUE = null;
            OCCLUSION_SHAPE_DEBUG_VALUE = null;
        }
        else
        {
            FabricConfigSpec.Builder builder = new FabricConfigSpec.Builder("framedblocks-devtools.toml");

            DOUBLE_BLOCK_PART_DEBUG_VALUE = builder
                    .comment(
                            "Enable double-block part debug renderer for testing whether FramedBlockEntity#hitSecondary() is correctly implemented.",
                            "Only applies to FramedBlocks blocks, add-on blocks are not handled by this."
                    )
                    .translation(TRANSLATION_DOUBLE_BLOCK_PART_DEBUG)
                    .define(KEY_DOUBLE_BLOCK_PART_DEBUG, false);
            CONNECTION_DEBUG_VALUE = builder
                    .comment(
                            "Enable debug renderer for testing ConnectionPredicates.",
                            "BlockEntities from add-ons must be registered to the renderer via AttachDebugRenderersEvent."
                    )
                    .translation(TRANSLATION_CONNECTION_DEBUG)
                    .define(KEY_CONNECTION_DEBUG, false);
            QUAD_WINDING_DEBUG_VALUE = builder
                    .comment(
                            "Enable quad-winding debug renderer to check for issues with quad winding on transformed quads.",
                            "BlockEntities from add-ons must be registered to the renderer via AttachDebugRenderersEvent."
                    )
                    .translation(TRANSLATION_QUAD_WINDING_DEBUG)
                    .define(KEY_QUAD_WINDING_DEBUG, false);
            STATE_MERGER_DEBUG_VALUE = builder
                    .comment("If enabled, all model wrapper registrations will print which state properties of the associated block are used as-is and which ones are handled by a StateMerger")
                    .translation(TRANSLATION_STATE_MERGER_DEBUG)
                    .define(KEY_STATE_MERGER_DEBUG, false);
            STATE_MERGER_DEBUG_FILTER_VALUE = builder
                    .comment(
                            "Set the regex pattern to filter the blocks for which StateMerger debug logging is enabled.",
                            "An empty string will disable filtering"
                    )
                    .translation(TRANSLATION_STATE_MERGER_DEBUG_FILTER)
                    .define(KEY_STATE_MERGER_DEBUG_FILTER, "", DevToolsConfig::validateFilterPattern);
            OCCLUSION_SHAPE_DEBUG_VALUE = builder
                    .comment("If enabled, switches block selection shape rendering to render the occlusion shape instead of the general shape")
                    .translation(TRANSLATION_OCCLUSION_SHAPE_DEBUG)
                    .define(KEY_OCCLUSION_SHAPE_DEBUG, false);

            SPEC = builder.build();
        }
    }

    private static boolean validateFilterPattern(Object value)
    {
        if (value instanceof String string)
        {
            if (string.isBlank()) return true;

            try
            {
                Pattern.compile(string);
                return true;
            }
            catch (PatternSyntaxException e)
            {
                return false;
            }
        }
        return false;
    }

    private static String translate(String key)
    {
        return Utils.translateConfig("devtools", key);
    }

    static void onConfigReloaded()
    {
            doubleBlockPartDebug = DOUBLE_BLOCK_PART_DEBUG_VALUE.get();
            connectionDebug = CONNECTION_DEBUG_VALUE.get();
            quadWindingDebug = QUAD_WINDING_DEBUG_VALUE.get();
            stateMergerDebug = STATE_MERGER_DEBUG_VALUE.get();
            String filter = STATE_MERGER_DEBUG_FILTER_VALUE.get();
            stateMergerDebugFilter = filter.isBlank() ? null : Pattern.compile(filter);
            occlusionShapeDebug = OCCLUSION_SHAPE_DEBUG_VALUE.get();
    }

    private DevToolsConfig() { }



    public static final class ViewImpl implements ExtConfigView.DevTools
    {
        private static final boolean IN_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();

        @Override
        public boolean isDoubleBlockPartHitDebugRendererEnabled()
        {
            return IN_DEV && doubleBlockPartDebug;
        }

        @Override
        public boolean isConnectionDebugRendererEnabled()
        {
            return IN_DEV && connectionDebug;
        }

        @Override
        public boolean isQuadWindingDebugRendererEnabled()
        {
            return IN_DEV && quadWindingDebug;
        }

        @Override
        public boolean isStateMergerDebugLoggingEnabled()
        {
            return IN_DEV && stateMergerDebug;
        }

        @Override
        @Nullable
        public Pattern getStateMergerDebugFilter()
        {
            return IN_DEV ? stateMergerDebugFilter : null;
        }

        @Override
        public boolean isOcclusionShapeDebugRenderingEnabled()
        {
            return IN_DEV && occlusionShapeDebug;
        }
    }
}
