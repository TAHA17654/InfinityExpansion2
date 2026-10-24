package com.balugaq.netex.utils;

import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Soft bridge to the compressed machines of InfinityExpansion2.
 * <p>
 * Usage in a grabber:
 * <pre>
 * boolean staged = CompressedBridge.stage(targetMenu);
 * try {
 *     // take the items out of the slots
 * } finally {
 *     if (staged) CompressedBridge.unstage(targetMenu);
 * }
 * </pre>
 * Everything is done with reflection, so Networks still works without InfinityExpansion2.
 */
public final class CompressedBridge {
    private static volatile boolean resolved = false;
    private static Method stageMethod = null;
    private static Method unstageMethod = null;

    private CompressedBridge() {
    }

    private static boolean resolve() {
        if (resolved) {
            return stageMethod != null;
        }

        synchronized (CompressedBridge.class) {
            if (resolved) {
                return stageMethod != null;
            }

            try {
                Plugin plugin = Bukkit.getPluginManager().getPlugin("InfinityExpansion2");
                if (plugin == null || !plugin.isEnabled()) {
                    // not loaded yet, try again later
                    return false;
                }

                Class<?> api = Class.forName(
                    "net.guizhanss.infinityexpansion2.api.CompressedBufferApi", true, plugin.getClass().getClassLoader());
                stageMethod = api.getMethod("stage", BlockMenu.class);
                unstageMethod = api.getMethod("unstage", BlockMenu.class);
            } catch (Throwable ignored) {
                // an old version of InfinityExpansion2 without the api
                stageMethod = null;
                unstageMethod = null;
            }

            resolved = true;
            return stageMethod != null;
        }
    }

    /**
     * Puts the buffered output of a compressed machine into its slots. Returns whether {@link #unstage} is needed.
     */
    public static boolean stage(BlockMenu menu) {
        if (menu == null || !resolve()) {
            return false;
        }

        try {
            return (Boolean) stageMethod.invoke(null, menu);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void unstage(BlockMenu menu) {
        if (menu == null || unstageMethod == null) {
            return;
        }

        try {
            unstageMethod.invoke(null, menu);
        } catch (Throwable ignored) {
            // nothing we can do
        }
    }
}
