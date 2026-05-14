package xfacthd.framedblocks.common.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import xfacthd.framedblocks.api.util.FramedConstants;
import xfacthd.framedblocks.common.datagen.providers.*;

import java.util.concurrent.CompletableFuture;

public final class GeneratorHandler
{
    public static void onGatherData(final Object /* GatherDataEvent */ event)
    {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        gen.addProvider(event.includeClient(), new FramedSpriteSourceProvider(output, lookupProvider, fileHelper));
        gen.addProvider(event.includeClient(), new FramedBlockStateProvider(output, fileHelper));
        gen.addProvider(event.includeClient(), new FramedItemModelProvider(output, fileHelper));
        gen.addProvider(event.includeServer(), new FramedLootTableProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), new FramedRecipeProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), new FramingSawRecipeProvider(output, lookupProvider));
        BlockTagsProvider tagProvider = new FramedBlockTagProvider(output, lookupProvider, fileHelper);
        gen.addProvider(event.includeServer(), tagProvider);
        gen.addProvider(event.includeServer(), new FramedItemTagProvider(output, lookupProvider, tagProvider.contentsGetter(), fileHelper));
        gen.addProvider(event.includeClient(), new FramedLanguageProvider(output));
    }



    private GeneratorHandler() { }
}