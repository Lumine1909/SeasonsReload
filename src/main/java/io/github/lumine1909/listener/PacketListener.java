package io.github.lumine1909.listener;

import io.github.lumine1909.event.PacketOutEvent;
import io.github.lumine1909.object.SeasonAccess;
import io.github.lumine1909.object.World;
import io.github.lumine1909.util.Wrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

import static io.github.lumine1909.Seasons.plugin;
import static io.github.lumine1909.util.Reflection.*;

public class PacketListener implements Listener {

    record BiomeData(BitStorage storage, Palette<Holder<Biome>> palette) {

        public void set(int x, int y, int z, Holder<Biome> biome) {
            int index = (y << 2 | z) << 2 | x;
            int i = palette.idFor(biome);
            storage.set(index, i);
        }

        public synchronized void write(FriendlyByteBuf buf) {
            buf.writeByte(storage.getBits());
            palette.write(buf);
            buf.writeLongArray(storage.getRaw());
        }
    }

    public PacketListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPacketOut(PacketOutEvent e) {
        Packet<?> p = (Packet<?>) e.getPacket();
        if (!Wrapper.of(e.getPlayer().getWorld()).hasSeason()) {
            return;
        }
        if (p instanceof ClientboundChunksBiomesPacket packet) {
            World world = Wrapper.of(e.getPlayer().getWorld());
            ServerLevel sw = world.getNMSWorld();
            List<ClientboundChunksBiomesPacket.ChunkBiomeData> datas = new ArrayList<>(packet.chunkBiomeData().size());
            packet.chunkBiomeData().forEach(c -> {
                LevelChunk chunk = sw.getChunk(c.pos().x, c.pos().z);
                byte[] buffer = new byte[calculateChunkSize(chunk) + 1024];
                FriendlyByteBuf buf = getWriteBuffer(buffer);
                extractChunkData(buf, chunk, world);
                ClientboundChunksBiomesPacket.ChunkBiomeData data = new ClientboundChunksBiomesPacket.ChunkBiomeData(c.pos(), buf.array());
                datas.add(data);
            });
            e.setPacket(new ClientboundChunksBiomesPacket(datas));
        } else if (p instanceof ClientboundLevelChunkWithLightPacket packet) {
            World world = Wrapper.of(e.getPlayer().getWorld());
            ServerLevel sw = world.getNMSWorld();
            ClientboundLevelChunkPacketData data = packet.getChunkData();
            int x = packet.getX(), z = packet.getZ();
            LevelChunk chunk = sw.getChunk(x, z);
            byte[] buffer = new byte[calculateChunkSize(chunk) + 1024];
            FriendlyByteBuf buf = getWriteBuffer(buffer);
            extractChunkData(buf, chunk, null, world);
            FClientboundLevelChunkPacketData_buffer.set(data, buf.array());
            FClientboundLevelChunkWithLightPacket_chunkData.set(packet, data);
            e.setPacket(packet);
            Bukkit.getScheduler().runTask(plugin, () -> Wrapper.of(e.getPlayer()).sendChunkBiome(x, z));
        }
    }

    private int calculateChunkSize(LevelChunk chunk) {
        int i = 0;
        for (LevelChunkSection levelChunkSection : chunk.getSections()) {
            i += levelChunkSection.getSerializedSize();
        }
        return i;
    }

    private FriendlyByteBuf getWriteBuffer(byte[] buffer) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(buffer);
        byteBuf.writerIndex(0);
        return new FriendlyByteBuf(byteBuf);
    }

    private void extractChunkData(FriendlyByteBuf buf, LevelChunk chunk, World world) {
        for (LevelChunkSection levelChunkSection : chunk.getSections()) {
            writeBiomes(buf, levelChunkSection, world);
        }
    }

    private void extractChunkData(FriendlyByteBuf buf, LevelChunk chunk, com.destroystokyo.paper.antixray.ChunkPacketInfo<net.minecraft.world.level.block.state.BlockState> chunkPacketInfo, World world) {
        int chunkSectionIndex = 0;

        for (LevelChunkSection levelChunkSection : chunk.getSections()) {
            buf.writeShort((short) FLevelChunkSection_nonEmptyBlockCount.get(levelChunkSection));
            levelChunkSection.states.write(buf, chunkPacketInfo, chunkSectionIndex);
            writeBiomes(buf, levelChunkSection, world);
            chunkSectionIndex++;
        }
    }

    private void writeBiomes(FriendlyByteBuf buf, LevelChunkSection levelChunkSection, World world) {
        PalettedContainer<Holder<Biome>> biomeContainer = (PalettedContainer<Holder<Biome>>) levelChunkSection.getBiomes();
        Object obj1 = FPalettedContainer_data.get(biomeContainer);
        BitStorage storage = ((BitStorage) FPalettedContainer_Data_storage.get(obj1)).copy();
        Palette<Holder<Biome>> palette = (Palette<Holder<Biome>>) FPalettedContainer_Data_palette.get(obj1);
        BiomeData data = new BiomeData(storage, palette);
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
}
