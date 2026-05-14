package xfacthd.framedblocks.client.model.compat;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.SimpleModelState}.
 */
public record SimpleModelState(Transformation rotation, boolean isUvLocked) implements ModelState
{
    @Override
    public Transformation getRotation() { return rotation; }
}
