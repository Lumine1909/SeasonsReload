package io.github.lumine1909.seasonsreload.listener;

import io.github.lumine1909.seasonsreload.injector.PacketInjector;
import io.github.lumine1909.seasonsreload.util.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static io.github.lumine1909.seasonsreload.SeasonsPlugin.plugin;

public class JoinQuitListener implements Listener {

    public JoinQuitListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Wrapper.of(e.getPlayer());
        PacketInjector.injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Wrapper.remove(e.getPlayer());
    }
}