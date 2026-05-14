package xfacthd.framedblocks.common.config.impl;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.fabricmc.loader.api.FabricLoader;
import xfacthd.framedblocks.FramedBlocks;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

/**
 * Minimal Fabric replacement for NeoForge's {@code ModConfigSpec} system.
 *
 * Uses Night Config (bundled with Fabric's dependencies via Cloth Config or directly
 * available as a transitive dep from minecraft itself in 1.21.1) to read/write TOML files.
 * The existing static-field + ViewImpl pattern in ClientConfig/ServerConfig/DevToolsConfig
 * is preserved exactly — only the builder/value API changes.
 *
 * Config files are placed in {@code .minecraft/config/framedblocks-<type>.toml}.
 */
public final class FabricConfigSpec
{
    private final String fileName;
    private final List<ValueEntry<?>> entries = new ArrayList<>();
    private CommentedFileConfig config;

    private FabricConfigSpec(String fileName)
    {
        this.fileName = fileName;
    }

    void addEntry(ValueEntry<?> entry)
    {
        entries.add(entry);
    }

    /** Load values from disk; create with defaults if missing. */
    public void load()
    {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        config = CommentedFileConfig.builder(configPath, TomlFormat.instance())
                .onFileNotFound(FileNotFoundAction.CREATE_EMPTY)
                .build();
        try
        {
            config.load();
        }
        catch (Exception e)
        {
            FramedBlocks.LOGGER.error("Failed to load config {}, using defaults", fileName, e);
        }

        // Apply defaults for any missing keys, then write back
        boolean needsSave = false;
        for (ValueEntry<?> entry : entries)
        {
            if (!config.contains(entry.path()))
            {
                config.set(entry.path(), entry.defaultValue());
                if (entry.comment() != null) config.setComment(entry.path(), entry.comment());
                needsSave = true;
            }
        }
        if (needsSave) config.save();

        // Push values into holders
        for (ValueEntry<?> entry : entries) entry.load(config);
    }

    public void reload()
    {
        if (config != null) load();
    }

    public String getFileName()
    {
        return fileName;
    }



    // ---- Builder ----

    public static final class Builder
    {
        private final FabricConfigSpec spec;
        private final Deque<String> path = new ArrayDeque<>();
        private String pendingComment = null;

        public Builder(String fileName)
        {
            this.spec = new FabricConfigSpec(fileName);
        }

        public Builder push(String category)
        {
            path.push(category);
            return this;
        }

        public Builder pop()
        {
            path.pop();
            return this;
        }

        public Builder comment(String... lines)
        {
            pendingComment = String.join("\n", lines);
            return this;
        }

        public Builder translation(String key)
        {
            return this; // translation keys stored for Cloth Config GUI (Step 8 / optional)
        }

        public Builder worldRestart()  { return this; }
        public Builder gameRestart()   { return this; }

        public BooleanValue define(String key, boolean defaultValue)
        {
            BooleanValue v = new BooleanValue(makePath(key), defaultValue, pendingComment);
            spec.addEntry(v);
            pendingComment = null;
            return v;
        }

        public IntValue defineInRange(String key, int defaultValue, int min, int max)
        {
            IntValue v = new IntValue(makePath(key), defaultValue, min, max, pendingComment);
            spec.addEntry(v);
            pendingComment = null;
            return v;
        }

        public <E extends Enum<E>> EnumValue<E> defineEnum(String key, E defaultValue)
        {
            EnumValue<E> v = new EnumValue<>(makePath(key), defaultValue, pendingComment);
            spec.addEntry(v);
            pendingComment = null;
            return v;
        }

        public StringListValue defineListAllowEmpty(
                String key, List<String> defaultValue,
                java.util.function.Supplier<String> elementSupplier,
                Predicate<Object> elementValidator
        )
        {
            StringListValue v = new StringListValue(makePath(key), defaultValue, elementValidator, pendingComment);
            spec.addEntry(v);
            pendingComment = null;
            return v;
        }

        public StringValue define(String key, String defaultValue, Predicate<Object> validator)
        {
            StringValue v = new StringValue(makePath(key), defaultValue, validator, pendingComment);
            spec.addEntry(v);
            pendingComment = null;
            return v;
        }

        public FabricConfigSpec build()
        {
            return spec;
        }

        private String makePath(String key)
        {
            if (path.isEmpty()) return key;
            StringJoiner joiner = new StringJoiner(".");
            // path is a stack — iterate in reverse (bottom to top)
            List<String> segments = new ArrayList<>(path);
            Collections.reverse(segments);
            segments.forEach(joiner::add);
            joiner.add(key);
            return joiner.toString();
        }
    }



    // ---- Value types ----

    public sealed interface ValueEntry<T> permits BooleanValue, IntValue, EnumValue, StringListValue, StringValue
    {
        String path();
        T defaultValue();
        String comment();
        void load(CommentedFileConfig config);
    }

    public static final class BooleanValue implements ValueEntry<Boolean>
    {
        private final String path;
        private final boolean defaultValue;
        private final String comment;
        private boolean value;

        BooleanValue(String path, boolean defaultValue, String comment)
        {
            this.path = path;
            this.defaultValue = defaultValue;
            this.comment = comment;
            this.value = defaultValue;
        }

        public boolean get() { return value; }

        @Override public String path() { return path; }
        @Override public Boolean defaultValue() { return defaultValue; }
        @Override public String comment() { return comment; }
        @Override public void load(CommentedFileConfig config)
        {
            value = config.getOrElse(path, defaultValue);
        }
    }

    public static final class IntValue implements ValueEntry<Integer>
    {
        private final String path;
        private final int defaultValue;
        private final int min;
        private final int max;
        private final String comment;
        private int value;

        IntValue(String path, int defaultValue, int min, int max, String comment)
        {
            this.path = path;
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
            this.comment = comment;
            this.value = defaultValue;
        }

        public int get() { return value; }

        @Override public String path() { return path; }
        @Override public Integer defaultValue() { return defaultValue; }
        @Override public String comment() { return comment; }
        @Override public void load(CommentedFileConfig config)
        {
            int raw = config.getOrElse(path, defaultValue);
            value = Math.max(min, Math.min(max, raw));
        }
    }

    public static final class EnumValue<E extends Enum<E>> implements ValueEntry<E>
    {
        private final String path;
        private final E defaultValue;
        private final String comment;
        private E value;

        EnumValue(String path, E defaultValue, String comment)
        {
            this.path = path;
            this.defaultValue = defaultValue;
            this.comment = comment;
            this.value = defaultValue;
        }

        public E get() { return value; }

        @Override public String path() { return path; }
        @Override public E defaultValue() { return defaultValue; }
        @Override public String comment() { return comment; }

        @Override
        @SuppressWarnings("unchecked")
        public void load(CommentedFileConfig config)
        {
            String raw = config.getOrElse(path, defaultValue.name());
            try
            {
                value = (E) Enum.valueOf(defaultValue.getClass(), raw.toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e)
            {
                value = defaultValue;
            }
        }
    }

    public static final class StringListValue implements ValueEntry<List<String>>
    {
        private final String path;
        private final List<String> defaultValue;
        private final Predicate<Object> validator;
        private final String comment;
        private List<String> value;

        StringListValue(String path, List<String> defaultValue, Predicate<Object> validator, String comment)
        {
            this.path = path;
            this.defaultValue = defaultValue;
            this.validator = validator;
            this.comment = comment;
            this.value = defaultValue;
        }

        public List<? extends String> get() { return value; }

        @Override public String path() { return path; }
        @Override public List<String> defaultValue() { return defaultValue; }
        @Override public String comment() { return comment; }
        @Override public void load(CommentedFileConfig config)
        {
            List<String> raw = config.getOrElse(path, defaultValue);
            value = raw.stream().filter(validator).toList();
        }
    }

    public static final class StringValue implements ValueEntry<String>
    {
        private final String path;
        private final String defaultValue;
        private final Predicate<Object> validator;
        private final String comment;
        private String value;

        StringValue(String path, String defaultValue, Predicate<Object> validator, String comment)
        {
            this.path = path;
            this.defaultValue = defaultValue;
            this.validator = validator;
            this.comment = comment;
            this.value = defaultValue;
        }

        public String get() { return value; }

        @Override public String path() { return path; }
        @Override public String defaultValue() { return defaultValue; }
        @Override public String comment() { return comment; }
        @Override public void load(CommentedFileConfig config)
        {
            String raw = config.getOrElse(path, defaultValue);
            value = validator.test(raw) ? raw : defaultValue;
        }
    }
}
