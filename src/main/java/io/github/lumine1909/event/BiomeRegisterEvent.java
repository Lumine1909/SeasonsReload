package io.github.lumine1909.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BiomeRegisterEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final String resourceLoc;
    private final Object biome;

    public BiomeRegisterEvent(String resourceLoc, Object biome) {
        this.resourceLoc = resourceLoc;
        this.biome = biome;
    }

    public Object getBiome() {
        return biome;
    }

    public String getResourceLoc() {
        return resourceLoc;
    }
}