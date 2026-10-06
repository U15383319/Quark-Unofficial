package org.violetmoon.zeta.util;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.Zeta;

import java.nio.file.Path;

//TODO: either move all this stuff in Zeta o move all Zeta related stuff here (like isModLoaded, client ticker and stuff)
// Either remove or move zeta helper stuff here.Forge specific behavior can be done with Platform delegates
public class Utils {
    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static Path modsDir() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }

    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    // modloader services
    public boolean isModLoaded(String modid){
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public @Nullable String getModDisplayName(String modid){
        return FabricLoader.getInstance().getModContainer(modid).map(container -> container.getMetadata().getName()).orElse(null);
    }
}
