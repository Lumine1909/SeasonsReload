package io.github.lumine1909.seasonsreload.util;

import io.github.lumine1909.seasonsreload.object.Player;
import io.github.lumine1909.seasonsreload.object.World;

import java.util.HashMap;
import java.util.Map;

public class Wrapper {

    private final static Map<org.bukkit.entity.Player, Player> playerMap = new HashMap<>();
    private final static Map<String, World> worldMap = new HashMap<>();

    public static Player of(org.bukkit.entity.Player player) {
        if (!playerMap.containsKey(player)) {
            playerMap.put(player, new Player(player));
        }
        return playerMap.get(player);
    }

    public static World of(String world) {
        if (!worldMap.containsKey(world)) {
            worldMap.put(world, new World(world));
        }
        return worldMap.get(world);
    }

    public static World of(org.bukkit.World world) {
        if (!worldMap.containsKey(world.getName())) {
            worldMap.put(world.getName(), new World(world));
        }
        return worldMap.get(world.getName());
    }

    public static void remove(org.bukkit.entity.Player player) {
        playerMap.remove(player);
    }
}