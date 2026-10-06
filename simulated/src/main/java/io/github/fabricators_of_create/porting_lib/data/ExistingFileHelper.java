package io.github.fabricators_of_create.porting_lib.data;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.FileNotFoundException;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Porting-Lib 没有 26.3 版本，本地桩实现（仅供编译，不做资源校验）。
 */
public class ExistingFileHelper {
    public static final String EXISTING_RESOURCES = "existing";
    public static final String EXISTING_MODS = "existing_mods";

    public ExistingFileHelper(final Collection<Path> packFiles, final Set<String> sourceContainers,
                              final boolean strict, final String modDir, final File assetsDir) {
    }

    public boolean exists(final Identifier id, final ResourceType type) {
        return true;
    }

    public boolean exists(final Identifier id, final String prefix, final String suffix) {
        return true;
    }

    public void trackGenerated(final Identifier id, final ResourceType type) {
    }

    public void trackGenerated(final Identifier id, final String prefix, final String suffix) {
    }

    public Resource getResource(final Identifier id, final String prefix, final String suffix) throws FileNotFoundException {
        return null;
    }

    public List<Resource> getResourceStack(final Identifier id) {
        return List.of();
    }

    public boolean isEnabled() {
        return true;
    }

    public enum ResourceType {
        CLIENT_RESOURCES,
        SERVER_DATA,
        BUNDLED_DATA
    }
}
