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
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static io.github.lumine1909.seasonsreload.SeasonsPlugin.plugin;
import static io.github.lumine1909.seasonsreload.util.Reflection.*;

@SuppressWarnings("unchecked")
public class PacketInjector {

    private static final String HANDLER_NAME = "seasons-handler";
    private static final MappedRegistry<Biome> REGISTRY = (MappedRegistry<Biome>) MinecraftServer.getServer().registryAccess().lookup(Registries.BIOME).orElseThrow();

    public static void inject() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    public static void uninject() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            uninjectPlayer(player);
        }
    }

    public static void injectPlayer(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = serverPlayer.connection.connection.channel;
        if (channel.pipeline().get(HANDLER_NAME) != null) {
            channel.pipeline().remove(HANDLER_NAME);
        }
        channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new PacketInterceptor(player));
    }

    private static void uninjectPlayer(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = serverPlayer.connection.connection.channel;
        if (channel.pipeline().get(HANDLER_NAME) != null) {
            channel.pipeline().remove(HANDLER_NAME);
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
        PalettedContainer<Holder<Biome>> container = (PalettedContainer<Holder<Biome>>) levelChunkSection.getBiomes();
        BitStorage storage = ((BitStorage) field$PalettedContainer$Data$storage.get(field$PalettedContainer$data.get(container))).copy();
        Object containerData = field$PalettedContainer$data.get(container);
        var palette = (Palette<Holder<Biome>>) field$PalettedContainer$Data$palette.get(containerData);

        buf.writeByte(storage.getBits());
        if (palette instanceof SingleValuePalette<Holder<Biome>> single) {
            buf.writeVarInt(getModifiedId((Holder<Biome>) field$SingleValuePalette$value.get(single), world));
        } else if (palette instanceof LinearPalette<Holder<Biome>> linear) {
            var array = (Holder<Biome>[]) field$LinearPalette$values.get(linear);
            buf.writeVarInt(linear.getSize());
            for (int i = 0; i < linear.getSize(); i++) {
                buf.writeVarInt(getModifiedId(array[i], world));
            }
        } else if (palette instanceof HashMapPalette<Holder<Biome>> hashMap) {
            var map = (CrudeIncrementalIntIdentityHashBiMap<Holder<Biome>>) field$HashMapPalette$values.get(hashMap);
            buf.writeVarInt(hashMap.getSize());
            for (int i = 0; i < hashMap.getSize(); i++) {
                buf.writeVarInt(getModifiedId(map.byId(i), world));
            }
        }
        buf.writeFixedSizeLongArray(storage.getRaw());
    }

    private static int getModifiedId(Holder<Biome> origin, World world) {
        return REGISTRY.getId(SeasonAccess.getFrom(origin.value()).get(plugin.server.getLevel(world).getSeason()).value());
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
}