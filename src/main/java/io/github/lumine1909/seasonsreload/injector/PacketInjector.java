package io.github.lumine1909.seasonsreload.injector;

import io.github.lumine1909.seasonsreload.object.SeasonAccess;
import io.github.lumine1909.seasonsreload.object.World;
import io.github.lumine1909.seasonsreload.util.Wrapper;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static io.github.lumine1909.seasonsreload.SeasonsPlugin.plugin;
import static io.github.lumine1909.seasonsreload.util.Reflection.*;

public class PacketInjector {

    private static final String handlerName = "seasons-handler";
    private static final Key key = Key.key("seasonsreload:handler");

    public static void inject() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    public static void uninject() {
        ChannelInitializeListenerHolder.removeListener(key);
        for (Player player : Bukkit.getOnlinePlayers()) {
            uninjectPlayer(player);
        }
    }

    public static void injectPlayer(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = serverPlayer.connection.connection.channel;
        if (channel.pipeline().get(handlerName) != null) {
            channel.pipeline().remove(handlerName);
        }
        channel.pipeline().addBefore("packet_handler", handlerName, new PacketInterceptor(player));
    }

    private static void uninjectPlayer(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = serverPlayer.connection.connection.channel;
        if (channel.pipeline().get(handlerName) != null) {
            channel.pipeline().remove(handlerName);
        }
    }

    private static void extractBiomeData(FriendlyByteBuf buf, LevelChunk chunk, World world) {
        for (LevelChunkSection levelChunkSection : chunk.getSections()) {
            writeBiomes(buf, levelChunkSection, world);
        }
    }

    private static void extractChunkData(FriendlyByteBuf buf, LevelChunk chunk, World world) {
        int chunkSectionIndex = 0;

        for (LevelChunkSection levelChunkSection : chunk.getSections()) {
            buf.writeShort((short) field$LevelChunkSection$nonEmptyBlockCount.get(levelChunkSection));
            levelChunkSection.states.write(buf, null, chunkSectionIndex);
            writeBiomes(buf, levelChunkSection, world);
            chunkSectionIndex++;
        }
    }

    private static void writeBiomes(FriendlyByteBuf buf, LevelChunkSection levelChunkSection, World world) {
        PalettedContainer<Holder<Biome>> biomeContainer = (PalettedContainer<Holder<Biome>>) levelChunkSection.getBiomes();
        BiomeData data = new BiomeData(biomeContainer);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    Biome origin = biomeContainer.get(i, j, k).value();
                    Holder<Biome> b = SeasonAccess.getFrom(origin).get(plugin.server.getLevel(world).getSeason());
                    data.set(i, j, k, b);
                }
            }
        }
        data.write(buf);
    }

    private static final class PacketInterceptor extends ChannelDuplexHandler {

        private final Player player;

        public PacketInterceptor(Player player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (player == null || !Wrapper.of(player.getWorld()).hasSeason()) {
                super.write(ctx, msg, promise);
                return;
            }
            if (msg instanceof ClientboundChunksBiomesPacket(
                List<ClientboundChunksBiomesPacket.ChunkBiomeData> chunkBiomeData
            )) {
                World world = Wrapper.of(player.getWorld());
                ServerLevel sw = world.getNMSWorld();
                List<ClientboundChunksBiomesPacket.ChunkBiomeData> dataList = new ArrayList<>(chunkBiomeData.size());
                chunkBiomeData.forEach(c -> {
                    LevelChunk chunk = sw.getChunk(c.pos().x, c.pos().z);
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    extractBiomeData(buf, chunk, world);
                    ClientboundChunksBiomesPacket.ChunkBiomeData data = new ClientboundChunksBiomesPacket.ChunkBiomeData(c.pos(), buf.array());
                    dataList.add(data);
                });
                msg = new ClientboundChunksBiomesPacket(dataList);
            } else if (msg instanceof ClientboundLevelChunkWithLightPacket packet) {
                World world = Wrapper.of(player.getWorld());
                ServerLevel sw = world.getNMSWorld();
                ClientboundLevelChunkPacketData data = packet.getChunkData();
                int x = packet.getX(), z = packet.getZ();
                LevelChunk chunk = sw.getChunk(x, z);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                extractChunkData(buf, chunk, world);
                field$ClientboundLevelChunkPacketData$buffer.set(data, ByteBufUtil.getBytes(buf));
            }
            super.write(ctx, msg, promise);
        }
    }

    record BiomeData(int[] temp, PalettedContainer<Holder<Biome>> container) {

        BiomeData(PalettedContainer<Holder<Biome>> container) {
            this(new int[64], container);
        }

        public void set(int x, int y, int z, Holder<Biome> biome) {
            int index = (y << 2 | z) << 2 | x;
            int id = getPalette().idFor(biome);
            temp[index] = id;
        }

        public synchronized void write(FriendlyByteBuf buf) {
            BitStorage storage = ((BitStorage) field$PalettedContainer$Data$storage.get(field$PalettedContainer$data.get(container))).copy();
            for (int i = 0; i < 64; i++) {
                storage.set(i, temp[i]);
            }
            buf.writeByte(storage.getBits());
            getPalette().write(buf);
            buf.writeFixedSizeLongArray(storage.getRaw());
        }

        private Palette<Holder<Biome>> getPalette() {
            Object containerData = field$PalettedContainer$data.get(container);
            return (Palette<Holder<Biome>>) field$PalettedContainer$Data$palette.get(containerData);
        }
    }
}