package io.github.lumine1909.seasonsreload.core;

import io.github.lumine1909.seasonsreload.object.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class GlobalSeasonServer extends BukkitRunnable {

    private final boolean globalSeed;
    private final Map<World, LevelSeasonServer> levelServers = new HashMap<>();
    private long seed;

    public GlobalSeasonServer(long seed, boolean globalSeed) {
        this.seed = seed;
        this.globalSeed = globalSeed;
    }

    public static GlobalSeasonServer spin(long seed, boolean globalSeed) {
        GlobalSeasonServer server = new GlobalSeasonServer(seed, globalSeed);
        return server;
    }

    public void addLevel(LevelSeasonServer seasonServer) {
        levelServers.put(seasonServer.getWorld(), seasonServer);
    }

    public void removeLevel(LevelSeasonServer seasonServer) {
        levelServers.remove(seasonServer.getWorld());
    }

    public LevelSeasonServer getLevel(World world) {
        return levelServers.get(world);
    }

    public void save(FileConfiguration config) {
        config.set("seed.global", seed);
        for (LevelSeasonServer server : levelServers.values()) {
            if (globalSeed) {
                config.set("seed.worlds." + server.getWorld().getWorld(), -1);
            } else {
                config.set("seed.worlds." + server.getWorld().getWorld(), server.getSeasonSeed());
            }
        }
    }

    @Override
    public void run() {
        for (LevelSeasonServer server : levelServers.values()) {
            server.tick(seed, globalSeed);
        }
        seed++;
        seed %= 8760000;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}