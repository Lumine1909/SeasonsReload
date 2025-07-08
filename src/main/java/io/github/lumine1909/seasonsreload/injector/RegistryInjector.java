package io.github.lumine1909.seasonsreload.injector;

import io.github.lumine1909.seasonsreload.object.SeasonAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Map;

public class RegistryInjector {

    public static void registerAllBiomes() {
        MappedRegistry<Biome> registry = (MappedRegistry<Biome>) MinecraftServer.getServer().registryAccess().lookup(Registries.BIOME).orElseThrow();
        ArrayList<Biome> biomes = new ArrayList<>(registry.entrySet().stream().map(Map.Entry::getValue).toList());
        for (Biome biome : biomes) {
            SeasonAccess.createFrom(biome);
        }
    }
}