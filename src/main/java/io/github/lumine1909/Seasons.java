package io.github.lumine1909;

import io.github.lumine1909.core.GlobalSeasonServer;
import io.github.lumine1909.core.LevelSeasonServer;
import io.github.lumine1909.injector.PacketListenerInjector;
import io.github.lumine1909.injector.RegistryInjector;
import io.github.lumine1909.listener.JoinQuitListener;
import io.github.lumine1909.listener.PacketListener;
import io.github.lumine1909.listener.RegistryListener;
import io.github.lumine1909.object.Date;
import io.github.lumine1909.object.World;
import io.github.lumine1909.util.Wrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;


public class Seasons extends JavaPlugin implements Listener {

    public static Seasons plugin;
    public GlobalSeasonServer server;

    public long SEED;
    public boolean DO_GLOBAL_TICK;

    @Override
    public void onLoad() {
        plugin = this;
        saveDefaultConfig();
        RegistryInjector.inject();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        new RegistryListener();
        new JoinQuitListener();
        new PacketListener();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketListenerInjector.inject(player);
        }
        SEED = getConfig().getLong("seed.global");
        DO_GLOBAL_TICK = getConfig().getBoolean("settings.global-season");
        server = GlobalSeasonServer.spin(SEED, DO_GLOBAL_TICK);
        Bukkit.getCommandMap().register("seasonsreload", new Command("biome") {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                if (!(sender instanceof Player player)) {
                    return true;
                }
                Location loc = player.getLocation();
                ServerLevel sl = Wrapper.of(player.getWorld()).getNMSWorld();
                sl.getChunkSource().chunkMap.resendBiomesForChunks(List.of(sl.getChunkAt(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))));
                return true;
            }
        });

        Bukkit.getCommandMap().register("seasonsreload", new Command("season") {

            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                if (!(sender instanceof Player player)) {
                    return true;
                }
                if (args.length == 1) {
                    if (args[0].equals("enable")) {
                        if (!player.hasPermission("seasons.enable")) {
                            player.sendMessage(ChatColor.RED + "你缺少执行此命令的权限");
                            return true;
                        }
                        World world = Wrapper.of(player.getWorld());
                        if (world.hasSeason()) {
                            player.sendMessage(ChatColor.RED + "当前世界已经启用季节功能了");
                            return true;
                        }
                        LevelSeasonServer level = new LevelSeasonServer(world);
                        long seed = getConfig().getLong("seed.worlds." + world.getWorld().getName(), 0);
                        level.setSeasonSeed(seed);
                        server.addLevel(level);
                        player.sendMessage(ChatColor.GREEN + "启用成功!");
                    } else if (args[0].equals("disable")) {
                        if (!player.hasPermission("seasons.disable")) {
                            player.sendMessage(ChatColor.RED + "你缺少执行此命令的权限");
                            return true;
                        }
                        World world = Wrapper.of(player.getWorld());
                        if (!world.hasSeason()) {
                            player.sendMessage(ChatColor.RED + "当前世界没有启用季节功能");
                            return true;
                        }
                        LevelSeasonServer level = server.getLevel(world);
                        if (DO_GLOBAL_TICK) {
                            getConfig().set("seed.worlds." + level.getWorld().getWorld().getName(), -1);
                        } else {
                            getConfig().set("seed.worlds." + level.getWorld().getWorld().getName(), level.getSeasonSeed());
                        }
                        saveConfig();
                        server.removeLevel(level);
                        world.setSeason(null);
                        player.sendMessage(ChatColor.AQUA + "禁用成功!");
                    }
                } else if (args.length == 2 && args[0].equals("date") && args[1].equals("get")) {
                    if (!player.hasPermission("seasons.date.get")) {
                        player.sendMessage(ChatColor.RED + "你缺少执行此命令的权限");
                        return true;
                    }
                    World world = Wrapper.of(player.getWorld());
                    if (!world.hasSeason()) {
                        player.sendMessage(ChatColor.RED + "当前世界没有启用季节功能");
                        return true;
                    }
                    LevelSeasonServer level = server.getLevel(world);
                    player.sendMessage(ChatColor.AQUA + "当前日期: " + Date.fromTickLong(level.getSeasonSeed()));
                } else if (args.length == 3 && args[0].equals("date") && args[1].equals("set")) {
                    if (!player.hasPermission("seasons.date.set")) {
                        player.sendMessage(ChatColor.RED + "你缺少执行此命令的权限");
                        return true;
                    }
                    World world = Wrapper.of(player.getWorld());
                    if (!world.hasSeason()) {
                        player.sendMessage(ChatColor.RED + "当前世界没有启用季节功能");
                        return true;
                    }
                    LevelSeasonServer level = server.getLevel(world);
                    try {
                        int month = Integer.parseInt(args[2].split("/")[0]);
                        int day = Integer.parseInt(args[2].split("/")[1]);
                        Date date = new Date(month, day);
                        if (DO_GLOBAL_TICK) {
                            server.setSeed(date.toLongSeed((int) world.getWorld().getTime()));
                        } else {
                            level.setSeasonSeed(date.toLongSeed((int) world.getWorld().getTime()));
                        }
                        player.sendMessage(ChatColor.AQUA + "成功设置当前日期为: " + date);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "日期格式不正确, 请使用 mm/dd");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "命令语法不正确, 请使用: ");
                    player.sendMessage(ChatColor.GREEN + "  /season enable|disable 启用/禁用功能");
                    player.sendMessage(ChatColor.GREEN + "  /season date get|set 获取/设置日期");
                    player.sendMessage(ChatColor.GREEN + "  日期格式: mm/dd");
                }
                return true;
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws IllegalArgumentException {
                if (args.length == 1) {
                    return List.of("enable", "disable", "date");
                }
                if (args.length == 2 && args[0].equals("date")) {
                    return List.of("get", "set");
                }
                return Collections.emptyList();
            }
        });
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.save(getConfig());
            saveConfig();
        }
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e) {
        if (e.getType() != ServerLoadEvent.LoadType.STARTUP) {
            getLogger().warning("You are trying to reload the server, it may cause some issue on this plugin");
        }
        ConfigurationSection cs = getConfig().getConfigurationSection("seed.worlds");
        if (cs == null) {
            cs = getConfig().createSection("seed.worlds");
        }
        for (String world : cs.getKeys(false)) {
            LevelSeasonServer level = new LevelSeasonServer(Wrapper.of(Bukkit.getWorld(world)));
            if (DO_GLOBAL_TICK) {
                level.setSeasonSeed(cs.getInt(world));
            }
            server.addLevel(level);
        }
    }
}