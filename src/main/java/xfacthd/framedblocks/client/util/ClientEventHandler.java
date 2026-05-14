package xfacthd.framedblocks.client.util;

import xfacthd.framedblocks.common.crafting.FramingSawRecipeCache;

public final class ClientEventHandler
{
    public static void onRecipesUpdated(net.minecraft.client.multiplayer.ClientPacketListener handler,
            net.minecraft.world.item.crafting.RecipeManager recipeManager)
    {
        FramingSawRecipeCache.get(true).update(recipeManager);
    }

    public static void onClientDisconnect()
    {
        FramingSawRecipeCache.get(true).clear();
    }



    private ClientEventHandler() { }
}
