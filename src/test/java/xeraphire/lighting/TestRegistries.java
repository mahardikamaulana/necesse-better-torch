package xeraphire.lighting;

import necesse.engine.registries.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestRegistries {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    public static synchronized void ensureCoreRegistries() {
        if (INITIALIZED.get()) {
            return;
        }

        try {
            GNDRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            DamageTypeRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            BuffRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            LevelLayerRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            ObjectLayerRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            RegionLayerRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            LevelDataRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            LevelRegistry.instance.registerCore();
            LevelRegistry.registerLevel("level", necesse.level.maps.Level.class);
        } catch (Throwable ignored) {}
        try {
            RecipeTechRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            TileRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            ObjectRegistry.instance.registerCore();
        } catch (Throwable ignored) {}
        try {
            ItemRegistry.instance.registerCore();
        } catch (Throwable ignored) {}

        try {
            LightingMod mod = new LightingMod();
            mod.preInit();
            mod.init();
        } catch (Throwable ignored) {}

        INITIALIZED.set(true);
    }
}
