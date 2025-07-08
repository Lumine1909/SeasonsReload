package io.github.lumine1909.seasonsreload.object;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static io.github.lumine1909.seasonsreload.util.Reflection.field$MappedRegistry$frozen;
import static io.github.lumine1909.seasonsreload.util.Reflection.field$MappedRegistry$unregIntrHolder;

public record SeasonAccess(Holder<Biome> spring, Holder<Biome> summer, Holder<Biome> autumn, Holder<Biome> winter) {

    private final static Map<Biome, SeasonAccess> seasonAccessMap = new HashMap<>();

    public static SeasonAccess getFrom(Biome base) {
        return seasonAccessMap.get(base);
    }

    public static SeasonAccess createFrom(Biome base) {
        Biome.BiomeBuilder builder = new Biome.BiomeBuilder()
            .downfall(base.climateSettings.downfall())
            .temperature(base.climateSettings.temperature())
            .generationSettings(base.getGenerationSettings())
            .hasPrecipitation(base.hasPrecipitation())
            .mobSpawnSettings(base.getMobSettings());
        BiomeSpecialEffects.Builder builder1 = new BiomeSpecialEffects.Builder()
            //.ambientAdditionsSound(base.getSpecialEffects().getAmbientAdditionsSettings().orElse(null))
            //.ambientLoopSound(base.getSpecialEffects().getAmbientLoopSoundEvent().orElse(null))
            //.ambientMoodSound(base.getSpecialEffects().getAmbientMoodSettings().orElse(null))
            //.backgroundMusic(base.getBackgroundMusic().orElse(null))
            //.ambientParticle(base.getSpecialEffects().getAmbientParticleSettings().orElse(null))
            .grassColorModifier(base.getSpecialEffects().getGrassColorModifier())
            .skyColor(base.getSkyColor())
            .fogColor(base.getFogColor())
            .waterFogColor(base.getWaterFogColor())
            .waterColor(base.getWaterColor());
        //TODO: Color map init
        //int baseGrassColor = base.getGrassColor(0, 0);
        //int baseFoliaColor = base.getFoliageColor();
        int baseFoliaColor = 7842607;
        int baseGrassColor = 9551193;
        int r, g, b;
        int[] grassColor = new int[4];
        int[] foliaColor = new int[4];
        r = baseFoliaColor >> 16 & 0xFF;
        g = baseFoliaColor >> 8 & 0xFF;
        b = baseFoliaColor & 0xFF;
        foliaColor[0] = (Math.clamp(r - 10, 0, 255) << 16) | (Math.clamp(g + 15, 0, 255) << 8) | Math.clamp(b, 0, 255);
        foliaColor[1] = (Math.clamp(r - 15, 0, 255) << 16) | (Math.clamp(g + 25, 0, 255) << 8) | Math.clamp(b, 0, 255);
        foliaColor[2] = (Math.clamp(r + 65, 0, 255) << 16) | (Math.clamp(g - 30, 0, 255) << 8) | Math.clamp(b - 20, 0, 255);
        foliaColor[3] = (Math.clamp(r + 95, 0, 255) << 16) | (Math.clamp(g + 65, 0, 255) << 8) | Math.clamp(b + 140, 0, 255);

        r = baseGrassColor >> 16 & 0xFF;
        g = baseGrassColor >> 8 & 0xFF;
        b = baseGrassColor & 0xFF;
        grassColor[0] = (Math.clamp(r - 10, 0, 255) << 16) | (Math.clamp(g + 20, 0, 255) << 8) | Math.clamp(b, 0, 255);
        grassColor[1] = (Math.clamp(r - 15, 0, 255) << 16) | (Math.clamp(g + 30, 0, 255) << 8) | Math.clamp(b, 0, 255);
        grassColor[2] = (Math.clamp(r + 60, 0, 255) << 16) | (Math.clamp(g - 30, 0, 255) << 8) | Math.clamp(b - 20, 0, 255);
        grassColor[3] = (Math.clamp(r + 95, 0, 255) << 16) | (Math.clamp(g + 60, 0, 255) << 8) | Math.clamp(b + 140, 0, 255);

        Holder<Biome> spring = registerOrGet(
            base,
            builder.specialEffects(
                builder1
                    .grassColorOverride(grassColor[0])
                    .foliageColorOverride(foliaColor[0]).build()
            ).build(), Type.SPRING
        );
        Holder<Biome> summer = registerOrGet(
            base,
            builder.specialEffects(
                builder1
                    .grassColorOverride(grassColor[1])
                    .foliageColorOverride(foliaColor[1]).build()
            ).build(), Type.SUMMER
        );
        Holder<Biome> autumn = registerOrGet(
            base,
            builder.specialEffects(
                builder1
                    .grassColorOverride(grassColor[2])
                    .foliageColorOverride(foliaColor[2]).build()
            ).build(), Type.AUTUMN
        );
        Holder<Biome> winter = registerOrGet(
            base,
            builder.specialEffects(
                builder1
                    .grassColorOverride(grassColor[3])
                    .foliageColorOverride(foliaColor[3]).build()
            ).build(), Type.WINTER
        );
        SeasonAccess access = new SeasonAccess(spring, summer, autumn, winter);
        seasonAccessMap.put(base, access);
        return access;
    }

    private static Holder<Biome> registerOrGet(Biome base, Biome biome, Type season) {
        MappedRegistry<Biome> registry = (MappedRegistry<Biome>) MinecraftServer.getServer().registryAccess().lookup(Registries.BIOME).orElseThrow();
        String name = registry.getResourceKey(base).orElseThrow().location().toString().replaceAll(":", "_") + "_" + season.name().toLowerCase();
        ResourceLocation newLocation = ResourceLocation.fromNamespaceAndPath("seasons", name);
        if (registry.get(newLocation).isPresent()) {
            return registry.get(newLocation).get();
        }
        field$MappedRegistry$unregIntrHolder.set(registry, new IdentityHashMap<>());
        field$MappedRegistry$frozen.set(registry, false);
        registry.createIntrusiveHolder(biome);
        Holder.Reference<Biome> biomeReference = registry.register(ResourceKey.create(Registries.BIOME, newLocation), biome, RegistrationInfo.BUILT_IN);
        field$MappedRegistry$frozen.set(registry, true);
        field$MappedRegistry$unregIntrHolder.set(registry, null);
        return biomeReference;
    }

    public Holder<Biome> get(Type season) {
        switch (season) {
            case SPRING -> {
                return spring();
            }
            case SUMMER -> {
                return summer();
            }
            case AUTUMN -> {
                return autumn();
            }
            case WINTER -> {
                return winter();
            }
            default -> throw new RuntimeException();
        }
    }

    public enum Type {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER
    }
}