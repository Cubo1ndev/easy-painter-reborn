package io.github.aws404.easypainter.custom;

import com.google.gson.Gson;
import io.github.aws404.easypainter.EasyPainter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomMotivesManager {

    private static final Gson GSON = new Gson();

    private final PersistentStateManager stateManager;

    public CustomMotivesManager(PersistentStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void reload(ResourceManager manager) {
        MotiveCacheState paintingStorage = MotiveCacheState.getOrCreate(this.stateManager);

        Map<Identifier, Resource> paintings =  manager.findResources("painting", s -> true);
        Set<Identifier> keySet = paintings.keySet();

        AtomicInteger id = new AtomicInteger();
        keySet.stream().filter(identifier -> identifier.getPath().contains(".json")).forEach(identifier -> {
            MotiveCacheState.Entry motiveCache = paintingStorage.getOrCreateEntry(identifier, GSON, manager, this.stateManager);
            CustomMotivesManager.registerOrReplace(motiveCache.getId(), new CustomMotive(motiveCache));
            id.incrementAndGet();
        });

        this.stateManager.save();

        EasyPainter.LOGGER.info("Loaded {} painting motives! ({} custom motives)", Registries.PAINTING_VARIANT.getIds().size(), id.get());
    }

    private static void registerOrReplace(Identifier id, CustomMotive motive) {
        if (Registries.PAINTING_VARIANT.containsId(id)) {
            EasyPainter.LOGGER.info("Replacing painting motive '{}'. Note that removing motives at reload is not supported.", id);
            Registry.register(Registries.PAINTING_VARIANT, id.toString(), motive);
            return;
        }

        Registry.register(Registries.PAINTING_VARIANT, id, motive);
    }

    public static class CustomMotive extends PaintingVariant {

        public final MotiveCacheState.Entry state;

        public CustomMotive(MotiveCacheState.Entry state) {
            super(state.blockWidth * 16, state.blockHeight * 16);
            this.state = state;
        }

        public ItemStack createMapItem(int x, int y) {
            ItemStack map = new ItemStack(Items.FILLED_MAP);
            NbtComponent nbt = map.get(DataComponentTypes.CUSTOM_DATA);
            if (nbt == null) {
                nbt = NbtComponent.of(new NbtCompound());
            }
            NbtCompound compound = nbt.getNbt();
            if (compound == null) {
                compound = new NbtCompound();
            }
            compound.putInt("map", state.mapIds[x][y]);
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, map, compound);
            //nbt.putInt("map", state.mapIds[x][y]);
            return map;
        }
    }

}
