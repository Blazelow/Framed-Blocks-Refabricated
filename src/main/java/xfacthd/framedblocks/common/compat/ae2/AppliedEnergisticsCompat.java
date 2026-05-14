package xfacthd.framedblocks.common.compat.ae2;

import net.minecraft.world.item.ItemStack;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Applied Energistics 2 compatibility stub.
 *
 * TODO (Step 8 - Compat): AE2 on Fabric uses its own API for crafting machines and patterns.
 *  NeoForge-specific pieces removed:
 *   - DeferredRegister / AttachmentType  → AE2 Fabric uses a different attachment system
 *   - RegisterCapabilitiesEvent          → AE2 Fabric exposes ICraftingMachine via its own lookup
 *   - FabricLoader.getInstance().isModLoaded()           → replaced with FabricLoader
 *
 *  Re-enable GuardedAccess once the AE2 Fabric API (appeng fabric) is added as a compileOnly dep
 *  and the CRAFTING_MACHINE registration is updated to use AE2's Fabric capability API.
 */
public final class AppliedEnergisticsCompat
{
    private static boolean loaded = false;

    public static void init()
    {
        if (FabricLoader.getInstance().isModLoaded("ae2"))
        {
            // TODO (Step 8): GuardedAccess.init();
            loaded = true;
        }
    }

    public static boolean isLoaded() { return loaded; }

    public static ItemStack makeBlankPatternStack()
    {
        // TODO (Step 8): return GuardedAccess.makeBlankPatternStack();
        return ItemStack.EMPTY;
    }

    public static ItemStack makeSawPatternStack()
    {
        // TODO (Step 8): return GuardedAccess.makeSawPatternStack();
        return ItemStack.EMPTY;
    }

    public static boolean isPattern(ItemStack stack, boolean encoded)
    {
        // TODO (Step 8): return GuardedAccess.isPattern(stack, encoded);
        return false;
    }

    public static ItemStack tryEncodePattern(ItemStack input, ItemStack[] additives, ItemStack output)
    {
        // TODO (Step 8): return GuardedAccess.tryEncodePattern(input, additives, output);
        return null;
    }



    private AppliedEnergisticsCompat() { }
}
