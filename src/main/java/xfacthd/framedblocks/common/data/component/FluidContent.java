package xfacthd.framedblocks.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Fabric replacement for NeoForge's {@code SimpleFluidContent}.
 * Stores a fluid type + an amount in milli-buckets (1000 mB = 1 bucket).
 *
 * TODO (Step 6 - Capabilities): Integrate with the Fabric Transfer API
 *  ({@code net.fabricmc.fabric.api.transfer.v1.fluid}) so FramedTankBlockEntity
 *  can participate in the fluid storage system properly.
 */
public record FluidContent(Fluid fluid, long amount)
{
    public static final long BUCKET_VOLUME = 1000L;
    public static final FluidContent EMPTY = new FluidContent(Fluids.EMPTY, 0L);

    public static final Codec<FluidContent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidContent::fluid),
            Codec.LONG.fieldOf("amount").forGetter(FluidContent::amount)
    ).apply(inst, FluidContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(net.minecraft.core.registries.Registries.FLUID), FluidContent::fluid,
            ByteBufCodecs.VAR_LONG, FluidContent::amount,
            FluidContent::new
    );

    public boolean isEmpty()
    {
        return fluid == Fluids.EMPTY || amount <= 0;
    }

    public FluidContent withAmount(long newAmount)
    {
        return new FluidContent(fluid, newAmount);
    }
}
