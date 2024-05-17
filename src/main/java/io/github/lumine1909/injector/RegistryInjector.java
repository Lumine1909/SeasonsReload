package io.github.lumine1909.injector;

import com.mojang.serialization.Lifecycle;
import io.github.lumine1909.event.BiomeRegisterEvent;
import io.github.lumine1909.object.SeasonAccess;
import io.github.lumine1909.util.Reflection;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.lumine1909.util.Reflection.*;

public class RegistryInjector {

    private static class InjectedMappedRegistry<T> extends MappedRegistry<T> {

        public InjectedMappedRegistry(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
            super(key, lifecycle);
        }

        @Override
        public Holder.@NotNull Reference<T> register(@NotNull ResourceKey<T> key, @NotNull T value, @NotNull RegistrationInfo info) {
            Holder.Reference<T> holder = super.register(key, value, info);
            if (value instanceof Biome biome) {
                Bukkit.getPluginManager().callEvent(new BiomeRegisterEvent(key.location().toString(), biome));
            }
            return holder;
        }

    }

    public static void inject() {
        RegistryAccess.ImmutableRegistryAccess frozen = (RegistryAccess.ImmutableRegistryAccess) MinecraftServer.getServer().registryAccess();
        MappedRegistry<Biome> registry = (MappedRegistry<Biome>) frozen.registryOrThrow(Registries.BIOME);
        MappedRegistry<Biome> injected = new InjectedMappedRegistry<>(registry.key(), registry.registryLifecycle());
        Reflection.copyFieldsForSubClass(registry, injected);
        Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries = new HashMap<>((Map<ResourceKey<? extends Registry<?>>, Registry<?>>) FImmutableRegistryAccess_registries.get(frozen));
        registries.put(Registries.BIOME, injected);
        FImmutableRegistryAccess_registries.set(frozen, Map.copyOf(registries));
        LayeredRegistryAccess<RegistryLayer> registryAccess = MinecraftServer.getServer().registries();
        FLayeredRegistryAccess_composite.set(registryAccess, frozen);
        ArrayList<RegistryAccess.Frozen> values = new ArrayList<>((List<RegistryAccess.Frozen>) FLayeredRegistryAccess_values.get(registryAccess));
        values.replaceAll(e -> {
            if (e.registry(Registries.BIOME).isPresent()) {
                return frozen.freeze();
            }
            return e;
        });
        registerAllBiomes();
    }

    private static void registerAllBiomes() {
        MappedRegistry<Biome> registry = (MappedRegistry<Biome>) MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        ArrayList<Biome> biomes = new ArrayList<>(registry.entrySet().stream().map(Map.Entry::getValue).toList());
        for (Biome biome : biomes) {
            SeasonAccess.createFrom(biome);
        }
    }
}