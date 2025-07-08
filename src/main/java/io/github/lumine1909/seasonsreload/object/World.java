package io.github.lumine1909.seasonsreload.object;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;

public class World {

    private final String world;
    private org.bukkit.World cachedBukkitWorld;
    private volatile SeasonAccess.Type season = null;

    public World(String world) {
        this.world = world;
    }

    public World(org.bukkit.World world) {
        this.world = world.getName();
        this.cachedBukkitWorld = world;
    }

    public String getWorld() {
        return world;
    }

    public org.bukkit.World getBukkitWorld() {
        if (cachedBukkitWorld != null) {
            return cachedBukkitWorld;
        }
        cachedBukkitWorld = Bukkit.getWorld(world);
        return cachedBukkitWorld;
    }

    public ServerLevel getNMSWorld() {
        return ((CraftWorld) getBukkitWorld()).getHandle();
    }

    public SeasonAccess.Type getSeason() {
        return season;
    }

    public void setSeason(SeasonAccess.Type season) {
        this.season = season;
    }

    public boolean hasSeason() {
        return season != null;
    }
}