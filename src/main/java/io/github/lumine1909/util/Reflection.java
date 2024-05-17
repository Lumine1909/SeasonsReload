package io.github.lumine1909.util;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Reflection {

    public static class FieldAccessor {

        private final Field field;

        public FieldAccessor(Class<?> clazz, String fieldName) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        public Object get(Object obj) {
            try {
                return field.get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object obj, Object value) {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ConstructorAccessor {

        private final Constructor<?> constructor;

        public ConstructorAccessor(Class<?> clazz, Class<?>... args) {
            try {
                constructor = clazz.getDeclaredConstructor(args);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public Object newInstance(Object... objects) {
            try {
                return constructor.newInstance(objects);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void copyFieldsForSubClass(Object from, Object to) {
        for (Field field : from.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Field field1 = to.getClass().getSuperclass().getDeclaredField(field.getName());
                field1.setAccessible(true);
                field1.set(to, field.get(from));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static void copyFields(Object from, Object to) {
        for (Field field : from.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Field field1 = to.getClass().getDeclaredField(field.getName());
                field1.setAccessible(true);
                field1.set(to, field.get(from));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public static Class<?> clazz(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final Class<RegistryAccess.ImmutableRegistryAccess> CImmutableRegistryAccess = RegistryAccess.ImmutableRegistryAccess.class;
    public static final Class<LayeredRegistryAccess> CLayeredRegistryAccess = LayeredRegistryAccess.class;
    public static final Class<MappedRegistry> CMappedRegistry = MappedRegistry.class;
    public static final Class<?> CPalettedContainer_Data = clazz("net.minecraft.world.level.chunk.PalettedContainer$Data");

    public static final FieldAccessor FImmutableRegistryAccess_registries = new FieldAccessor(CImmutableRegistryAccess, "registries");
    public static final FieldAccessor FLayeredRegistryAccess_values = new FieldAccessor(CLayeredRegistryAccess, "values");
    public static final FieldAccessor FLayeredRegistryAccess_composite = new FieldAccessor(CLayeredRegistryAccess, "composite");
    public static final FieldAccessor FMappedRegistry_frozen = new FieldAccessor(CMappedRegistry, "frozen");
    public static final FieldAccessor FMappedRegistry_unreginstholder = new FieldAccessor(CMappedRegistry, "unregisteredIntrusiveHolders");
    public static final FieldAccessor FPalettedContainer_data = new FieldAccessor(PalettedContainer.class, "data");
    public static final FieldAccessor FPalettedContainer_Data_storage = new FieldAccessor(CPalettedContainer_Data, "storage");
    public static final FieldAccessor FPalettedContainer_Data_palette = new FieldAccessor(CPalettedContainer_Data, "palette");
    public static final FieldAccessor FClientboundLevelChunkWithLightPacket_chunkData = new FieldAccessor(ClientboundLevelChunkWithLightPacket.class, "chunkData");
    public static final FieldAccessor FClientboundLevelChunkWithLightPacket_x = new FieldAccessor(ClientboundLevelChunkWithLightPacket.class, "x");
    public static final FieldAccessor FClientboundLevelChunkWithLightPacket_z = new FieldAccessor(ClientboundLevelChunkWithLightPacket.class, "z");
    public static final FieldAccessor FClientboundLevelChunkPacketData_buffer = new FieldAccessor(ClientboundLevelChunkPacketData.class, "buffer");
    public static final FieldAccessor FLevelChunkSection_nonEmptyBlockCount = new FieldAccessor(LevelChunkSection.class, "nonEmptyBlockCount");

}