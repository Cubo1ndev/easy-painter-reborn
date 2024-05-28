package com.github.hexarubik.easypainter;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import com.github.hexarubik.easypainter.command.EasyPainterCommand;
import com.github.hexarubik.easypainter.custom.CustomFrameEntity;
import com.github.hexarubik.easypainter.custom.CustomMotivesManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasyPainter implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final TagKey<Block> PAINTING_IGNORED = TagKey.of(RegistryKeys.BLOCK, new Identifier("easy_painter:painting_ignored"));
    public static final TagKey<Block> CANNOT_SUPPORT_PAINTING = TagKey.of(RegistryKeys.BLOCK, new Identifier("easy_painter:cannot_support_painting"));
    public static final TagKey<EntityType<?>> PAINTING_INTERACT = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier("easy_painter:painting_interact"));

    public static final EntityType<CustomFrameEntity> CUSTOM_ITEM_FRAME_ENTITY_TYPE = Registry.register(Registries.ENTITY_TYPE, new Identifier("easy_painter", "custom_item_frame"), EntityType.Builder.<CustomFrameEntity>create(CustomFrameEntity::new, SpawnGroup.MISC).disableSaving().disableSummon().makeFireImmune().dimensions(1, 1).build());
    public static CustomMotivesManager customMotivesManager;

    public static void reloadSources(ServerWorld world, ResourceManager manager) {
        EasyPainter.customMotivesManager.reload(world, manager);
    }

    /**
     * Tests if the painting could fit the supplied motive
     *
     * @param entity the painting entity
     * @param motive the motive to test
     * @return <code>true</code> if the motive will fit
     */
    public static boolean canPaintingAttach(PaintingEntity entity, PaintingVariant motive) {
        Direction facing = entity.getHorizontalFacing();
        Direction rotated = entity.getHorizontalFacing().rotateYCounterclockwise();

        int widthPixels = motive.getWidth();
        int heightPixels = motive.getHeight();
        int widthBlocks = widthPixels / 16;
        int heightBlocks = heightPixels / 16;

        int attachX = (widthBlocks - 1) / -2;
        int attachY = (heightBlocks - 1) / -2;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < widthBlocks; x++) {
            for (int y = 0; y < heightBlocks; y++) {
                mutable.set(entity.getDecorationBlockPos()).move(rotated, x + attachX).move(Direction.UP, y + attachY);
                BlockState inside = entity.getWorld().getBlockState(mutable);

                mutable.move(facing.getOpposite());
                BlockState behind = entity.getWorld().getBlockState(mutable);

                if (!inside.isIn(PAINTING_IGNORED) || behind.isIn(CANNOT_SUPPORT_PAINTING)) {
                    return false;
                }
            }
        }

        return entity.getWorld().getOtherEntities(entity, getBoundingBox(entity, motive, facing, rotated, widthPixels, heightPixels), entity1 -> entity1.getType().isIn(PAINTING_INTERACT)).isEmpty();
    }

    private static Box getBoundingBox(PaintingEntity entity, PaintingVariant motive, Direction facing, Direction rotated, int widthPixels, int heightPixels) {
        if (entity.getVariant().value() == motive) {
            return entity.getBoundingBox();
        } else {
            double widthOffset = getOffset(widthPixels);
            double startX = (double) entity.getDecorationBlockPos().getX() + 0.5D - facing.getOffsetX() * 0.46875D + widthOffset * rotated.getOffsetX();
            double startY = (double) entity.getDecorationBlockPos().getY() + 0.5D + getOffset(heightPixels);
            double startZ = (double) entity.getDecorationBlockPos().getZ() + 0.5D - facing.getOffsetZ() * 0.46875D + widthOffset * rotated.getOffsetZ();

            double boxX = (facing.getAxis() == Direction.Axis.Z ? widthPixels : 1) / 32.0;
            double boxHeight = heightPixels / 32.0;
            double boxZ = (facing.getAxis() == Direction.Axis.X ? widthPixels : 1) / 32.0;

            return new Box(startX - boxX, startY - boxHeight, startZ - boxZ, startX + boxX, startY + boxHeight, startZ + boxZ);
        }

    }

    public static MutableText getPaintingDisplayName(Identifier id) {
        String translationKey = Util.createTranslationKey("painting", id);
        //if (ServerTranslations.INSTANCE.getLanguage(translationKey) != null) {
        return Text.translatable(translationKey + ".title");
        //}

        //return Text.literal(StringUtils.capitalize(id.getPath().replace("_", " ")));
    }

    public static MutableText getPaintingAuthor(Identifier id) {
        String translationKey = Util.createTranslationKey("painting", id);
        return Text.translatable(translationKey + ".author");
    }

    private static double getOffset(int i) {
        return i % 32 == 0 ? 0.5D : 0.0D;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Starting Easy Painter (Version {})", FabricLoader.getInstance().getModContainer("easy_painter").orElseThrow(() -> new IllegalStateException("initialising unloaded mod")).getMetadata().getVersion().getFriendlyString());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> EasyPainterCommand.register(dispatcher));

        PolymerEntityUtils.registerType(CUSTOM_ITEM_FRAME_ENTITY_TYPE);
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                EasyPainter.customMotivesManager = new CustomMotivesManager(world.getPersistentStateManager());
                reloadSources(world, server.getResourceManager());
            }
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            EasyPainter.reloadSources(server.getWorld(World.OVERWORLD), resourceManager);
        });
    }
}
