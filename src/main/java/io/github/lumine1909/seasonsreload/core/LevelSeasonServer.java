package io.github.lumine1909.seasonsreload.core;

import io.github.lumine1909.seasonsreload.object.Date;
import io.github.lumine1909.seasonsreload.object.SeasonAccess;
import io.github.lumine1909.seasonsreload.object.World;

public class LevelSeasonServer {

    private final World world;
    private long seasonSeed;

    public LevelSeasonServer(World level) {
        world = level;
        seasonSeed = 0;
    }

    public void tick(long seed, boolean doGlobalTick) {
        if (doGlobalTick) {
            seasonSeed = seed - 1;
        }
        seasonSeed++;
        seasonSeed %= 8760000;
        SeasonAccess.Type season = Date.fromTickLong(seasonSeed).getSeason();
        if (this.world.getSeason() == season) {
            return;
        }
        world.setSeason(season);
        //TODO: world season effects and events
    }

    public SeasonAccess.Type getSeason() {
        return world.getSeason();
    }

    public World getWorld() {
        return world;
    }

    public long getSeasonSeed() {
        return seasonSeed;
    }

    public void setSeasonSeed(long seasonSeed) {
        this.seasonSeed = seasonSeed;
    }
}