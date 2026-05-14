package xfacthd.framedblocks.common.util.registration;

import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xfacthd.framedblocks.api.util.FramedConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Fabric replacement for NeoForge's DeferredRegister.
 *
 * Collects (name, factory) pairs and registers them all when {@link #registerAll(Registry)} is
 * called from the mod's {@code onInitialize()} entrypoint.
 *
 * Supports two usage patterns:
 * <ol>
 *   <li>{@link #register(String, Supplier)} — creates and returns a new {@link RegistryHolder}.</li>
 *   <li>{@link #register(RegistryHolder, Supplier)} — accepts a pre-created subclass holder
 *       (e.g. {@link xfacthd.framedblocks.api.util.registration.DeferredBlockEntity}) so the
 *       caller controls the concrete type while this registrar handles the actual registration.</li>
 * </ol>
 */
public final class FabricRegistrar<T>
{
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final List<Entry<T>> pending = new ArrayList<>();

    private FabricRegistrar(ResourceKey<? extends Registry<T>> registryKey, String namespace)
    {
        this.registryKey = registryKey;
        this.namespace = namespace;
    }

    /** Register by name; creates and returns a fresh {@link RegistryHolder}. */
    public <V extends T> RegistryHolder<T, V> register(String name, Supplier<V> factory)
    {
        RegistryHolder<T, V> holder = new RegistryHolder<>(makeKey(name));
        return register(holder, factory);
    }

    /**
     * Register using a pre-created holder (e.g. a typed subclass).
     * The holder will be bound after {@link #registerAll(Registry)} completes.
     */
    public <V extends T> RegistryHolder<T, V> register(RegistryHolder<T, V> holder, Supplier<V> factory)
    {
        pending.add(new Entry<>(holder, castFactory(factory)));
        return holder;
    }

    /** Execute all queued registrations. Must be called during mod init. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerAll(Registry<T> registry)
    {
        WritableRegistry<T> writable = (WritableRegistry<T>) registry;
        for (Entry<T> entry : pending)
        {
            T value = entry.factory().get();
            ResourceKey<T> key = (ResourceKey<T>) entry.holder().getKey();
            var ref = writable.register(key, value, RegistrationInfo.BUILT_IN);
            ((RegistryHolder) entry.holder()).bind(ref);
        }
        pending.clear();
    }

    public String getNamespace()
    {
        return namespace;
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey()
    {
        return registryKey;
    }

    @SuppressWarnings("unchecked")
    private <V extends T> ResourceKey<T> makeKey(String name)
    {
        return (ResourceKey<T>) ResourceKey.create(registryKey,
                ResourceLocation.fromNamespaceAndPath(namespace, name));
    }

    @SuppressWarnings("unchecked")
    private static <T> Supplier<T> castFactory(Supplier<? extends T> factory)
    {
        return (Supplier<T>) factory;
    }

    private record Entry<T>(RegistryHolder<T, ?> holder, Supplier<T> factory) { }



    public static <T> FabricRegistrar<T> create(ResourceKey<? extends Registry<T>> registryKey, String namespace)
    {
        return new FabricRegistrar<>(registryKey, namespace);
    }

    public static <T> FabricRegistrar<T> create(Registry<T> registry, String namespace)
    {
        return create(registry.key(), namespace);
    }

    public static <T> FabricRegistrar<T> forMod(ResourceKey<? extends Registry<T>> registryKey)
    {
        return create(registryKey, FramedConstants.MOD_ID);
    }
}
