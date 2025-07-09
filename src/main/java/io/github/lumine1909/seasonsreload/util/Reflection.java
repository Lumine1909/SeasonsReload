package io.github.lumine1909.seasonsreload.util;

import net.minecraft.core.MappedRegistry;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Reflection {

    public static final Class<MappedRegistry> class$MappedRegistry = MappedRegistry.class;
    public static final Class<PalettedContainer.Data> class$PalettedContainer$Data = PalettedContainer.Data.class;
    public static final Class<SingleValuePalette> class$SingleValuePalette = SingleValuePalette.class;
    public static final Class<LinearPalette> class$LinearPalette = LinearPalette.class;
    public static final Class<HashMapPalette> class$HashMapPalette = HashMapPalette.class;
    public static final FieldAccessor field$MappedRegistry$frozen = new FieldAccessor(class$MappedRegistry, "frozen");
    public static final FieldAccessor field$MappedRegistry$unregIntrHolder = new FieldAccessor(class$MappedRegistry, "unregisteredIntrusiveHolders");
    public static final FieldAccessor field$PalettedContainer$data = new FieldAccessor(PalettedContainer.class, "data");
    public static final FieldAccessor field$PalettedContainer$Data$storage = new FieldAccessor(class$PalettedContainer$Data, "storage");
    public static final FieldAccessor field$PalettedContainer$Data$palette = new FieldAccessor(class$PalettedContainer$Data, "palette");
    public static final FieldAccessor field$ClientboundLevelChunkPacketData$buffer = new FieldAccessor(ClientboundLevelChunkPacketData.class, "buffer");
    public static final FieldAccessor field$LevelChunkSection$nonEmptyBlockCount = new FieldAccessor(LevelChunkSection.class, "nonEmptyBlockCount");
    public static final FieldAccessor field$SingleValuePalette$value = new FieldAccessor(class$SingleValuePalette, "value");
    public static final FieldAccessor field$LinearPalette$values = new FieldAccessor(class$LinearPalette, "values");
    public static final FieldAccessor field$HashMapPalette$values = new FieldAccessor(class$HashMapPalette, "values");

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

}