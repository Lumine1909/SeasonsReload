package io.github.lumine1909.object;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.craftbukkit.CraftWorld;

public class World {

    private final org.bukkit.World world;
    private SeasonAccess.Type season = null;

    public World(org.bukkit.World world) {
        this.world = world;
    }

    public ServerLevel getNMSWorld() {
        return ((CraftWorld) world).getHandle();
    }

    public org.bukkit.World getWorld() {
        return world;
    }

    public void setSeason(SeasonAccess.Type season) {
        this.season = season;
    }

    public SeasonAccess.Type getSeason() {
        return season;
    }

    public boolean hasSeason() {
        return season != null;
    }
}