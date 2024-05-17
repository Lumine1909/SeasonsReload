package io.github.lumine1909.injector;

import io.github.lumine1909.event.PacketInEvent;
import io.github.lumine1909.event.PacketOutEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static io.github.lumine1909.Seasons.plugin;

public class PacketListenerInjector {

    private static final class PacketInterceptor extends ChannelDuplexHandler {

        public PacketInterceptor(Player player) {
            this.player = player;
        }

        public volatile Player player;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                msg = onPacketInAsync(player, msg);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in onPacketInAsync().", e);
            }
            if (msg != null) {
                super.channelRead(ctx, msg);
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            try {
                msg = onPacketOutAsync(player, msg);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in onPacketOutAsync().", e);
            }
            if (msg != null) {
                super.write(ctx, msg, promise);
            }
        }
    }

    private final static String handlerName = "seasons-handler";

    public static void inject(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = serverPlayer.connection.connection.channel;
        channel.pipeline().addBefore("packet_handler", handlerName, new PacketInterceptor(player));
    }

    private static Object onPacketInAsync(Player player, Object packet) {
        PacketInEvent event = new PacketInEvent(packet, player);
        Bukkit.getPluginManager().callEvent(event);
        return event.getPacket();
    }

    private static Object onPacketOutAsync(Player player, Object packet) {
        PacketOutEvent event = new PacketOutEvent(packet, player);
        Bukkit.getPluginManager().callEvent(event);
        return event.getPacket();
    }
}