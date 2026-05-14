package xfacthd.framedblocks.client.model.compat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.geometry.IGeometryLoader<T>}.
 * Reads model geometry from JSON during model loading.
 * Registered via our {@link ModelLoadingPlugin}.
 */
public interface IGeometryLoader<T extends IUnbakedGeometry<T>>
{
    T read(JsonObject modelContents, JsonDeserializationContext ctx) throws JsonParseException;
}
