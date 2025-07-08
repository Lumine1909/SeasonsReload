package io.github.lumine1909.seasonsreload.object;

import io.github.lumine1909.seasonsreload.util.Wrapper;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.List;

public class Player {

    private final org.bukkit.entity.Player player;

    public Player(org.bukkit.entity.Player player) {
        this.player = player;
    }

    public ServerPlayer getNMSPlayer() {
        return ((CraftPlayer) player).getHandle();
    }

    public void sendChunkBiome(int x, int z) {
        ServerLevel sl = getNMSPlayer().level();
        getNMSPlayer().connection.send(ClientboundChunksBiomesPacket.forChunks(List.of(sl.getChunk(x, z))));
    }

    public void sendChunkBiome(Chunk chunk) {
        sendChunkBiome(chunk.x(), chunk.z());
    }

    public Chunk getChunk() {
        World world = Wrapper.of(player.getWorld());
        int x = player.getLocation().getBlockX() >> 4;
        int z = player.getLocation().getBlockZ() >> 4;
        return new Chunk(world, x, z);
    }

    public void updateBiomes(int radius) {
        Chunk chunk = getChunk();
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                sendChunkBiome(chunk.x() + i, chunk.z() + j);
            }
        }
    }
}