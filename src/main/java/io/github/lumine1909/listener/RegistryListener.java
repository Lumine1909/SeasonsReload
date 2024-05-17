package io.github.lumine1909.listener;

import io.github.lumine1909.event.BiomeRegisterEvent;
import io.github.lumine1909.object.SeasonAccess;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static io.github.lumine1909.Seasons.plugin;

public class RegistryListener implements Listener {

    public RegistryListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRegistry(BiomeRegisterEvent e) {
        if (e.getResourceLoc().startsWith("seasons:")) {
            return;
        }
        SeasonAccess.createFrom((Biome) e.getBiome());
    }
}