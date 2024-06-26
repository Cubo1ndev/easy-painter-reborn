package com.github.hexarubik.easypainter.custom;

import com.github.hexarubik.easypainter.EasyPainter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MotiveCacheState extends PersistentState {

    private final HashMap<Identifier, Entry> entries;
    private final AtomicInteger currentMapId;

    public MotiveCacheState(HashMap<Identifier, Entry> entries, int currentMapId) {
        this.entries = entries;
        this.currentMapId = new AtomicInteger(currentMapId);
        this.markDirty();
    }

    public MotiveCacheState() {
        this(new HashMap<>(), 0);
    }

    public static MotiveCacheState getOrCreate(PersistentStateManager manager) {
        return manager.getOrCreate(getPersistentStateType(), "custom_motives");
    }

    public static PersistentState.Type<MotiveCacheState> getPersistentStateType() {
        return new PersistentState.Type<>(MotiveCacheState::new, MotiveCacheState::fromNbt, null);
    }

    public static MotiveCacheState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        int mapId = nbt.getInt("currentMapId");
        nbt.remove("currentMapId");
        HashMap<Identifier, Entry> entries = new HashMap<>();
        nbt.getKeys().stream().map(s -> Entry.fromNbt(nbt.getCompound(s))).forEach(entry -> entries.put(entry.id, entry));
        return new MotiveCacheState(entries, mapId);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbt.putInt("currentMapId", currentMapId.get());
        for (Entry entry : entries.values()) {
            if (entry != null)
                nbt.put(entry.id.toString(), entry.writeNbt(new NbtCompound()));
        }
        return nbt;
    }

    public Set<Identifier> getKeys() {
        return this.entries.keySet();
    }

    /**
     * Server must be stopped for this or it will crash
     */
    public void removeEntry(PersistentStateManager stateManager, Identifier id) {
        for (int[] mapId : entries.get(id).mapIds) {
            for (int i : mapId) {
                stateManager.set("map_" + i, getOrCreate(stateManager));
            }
        }

        this.entries.put(id, null);
        this.markDirty();
    }

    public Entry getOrCreateEntry(World world, Identifier resource, Gson gson, ResourceManager manager) {
        Identifier motiveId = new Identifier(resource.getNamespace(), resource.getPath().substring(9, resource.getPath().indexOf(".json")));

        if (this.entries.containsKey(motiveId)) {
            return this.entries.get(motiveId);
        }

        try {
            EasyPainter.LOGGER.info("Creating new painting motive '{}' for the first time", motiveId);

            Optional<Resource> op = manager.getResource(resource);
            if (op.isEmpty()) throw new IllegalArgumentException("Empty");
            JsonObject data = gson.fromJson(new InputStreamReader(op.get().getInputStream()), JsonObject.class);

            int blockWidth = data.get("blockWidth").getAsInt();
            int blockHeight = data.get("blockHeight").getAsInt();
            int customModelData = data.has("CustomModelData") ? data.get("CustomModelData").getAsInt() : 0;
            String imageName = data.has("image") ? "painting/" + data.get("image").getAsString() + ".png" : resource.getPath().substring(0, resource.getPath().indexOf(".json")) + "_image.png";
            ImageRenderer.DitherMode ditherMode = data.has("ditherMode") ? ImageRenderer.DitherMode.fromString(data.get("ditherMode").getAsString()) : ImageRenderer.DitherMode.NONE;

            Optional<Resource> op2 = manager.getResource(new Identifier(resource.getNamespace(), imageName));
            if (op2.isEmpty()) throw new IllegalArgumentException("Empty 2");
            BufferedImage image = ImageIO.read(op2.get().getInputStream());

            if (blockWidth / blockHeight != image.getWidth() / image.getHeight()) {
                throw new IllegalArgumentException("The image's height/width ratio is not the same as the supplied block dimensions");
            }

            int[][] mapIds = new int[blockWidth][blockHeight];

            Image resultingImage = image.getScaledInstance(128 * blockWidth, 128 * blockHeight, Image.SCALE_DEFAULT);
            for (int bW = 0; bW < blockWidth; bW++) {
                for (int bH = 0; bH < blockHeight; bH++) {
                    BufferedImage outputImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
                    outputImage.getGraphics().drawImage(resultingImage, 0, 0, 128, 128, bW * 128, bH * 128, (bW + 1) * 128, (bH + 1) * 128, null);
                    mapIds[bW][bH] = ImageRenderer.renderImageToMap(world, outputImage, ditherMode);
                }
            }

            Entry entry = new Entry(motiveId, blockWidth, blockHeight, customModelData, mapIds);
            entries.put(motiveId, entry);
            this.markDirty();
            return entry;
        } catch (IOException e) {
            EasyPainter.LOGGER.error("Error loading the custom painting '{}'. Error: ", motiveId);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        return null;
    }

    public static class Entry {

        public final int blockWidth;
        public final int blockHeight;
        public final int customModelData;
        public final int[][] mapIds;
        private final Identifier id;

        public Entry(Identifier id, int blockWidth, int blockHeight, int customModelData, int[][] mapIds) {
            this.id = id;
            this.blockWidth = blockWidth;
            this.blockHeight = blockHeight;
            this.mapIds = mapIds;
            this.customModelData = customModelData;
        }

        public static Entry fromNbt(NbtCompound nbt) {
            int[][] mapIds = new int[nbt.getInt("blockWidth")][nbt.getInt("blockHeight")];
            NbtList list = nbt.getList("mapIds", NbtElement.INT_ARRAY_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtIntArray arr = (NbtIntArray) list.get(i);
                for (int i1 = 0; i1 < arr.size(); i1++) {
                    mapIds[i][i1] = arr.get(i1).intValue();
                }
            }

            return new Entry(new Identifier(nbt.getString("id")), nbt.getInt("blockWidth"), nbt.getInt("blockHeight"), nbt.getInt("CustomModelData"), mapIds);
        }

        public Identifier getId() {
            return id;
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.putString("id", id.toString());
            nbt.putInt("blockWidth", blockWidth);
            nbt.putInt("blockHeight", blockHeight);
            nbt.putInt("CustomModelData", customModelData);

            NbtList list = new NbtList();
            for (int[] mapId : mapIds) {
                list.add(new NbtIntArray(mapId));
            }

            nbt.put("mapIds", list);

            return nbt;
        }
    }
}
