package io.github.aws404.easypainter;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Objects;

public class PaintingItem extends DecorationItem {

    public PaintingItem(Settings settings) {
        super(EntityType.PAINTING, settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        MutableText text = (MutableText) super.getName(stack);
        NbtCompound entityTag = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt();
        Identifier current = Identifier.tryParse(entityTag.getString("Motive"));
        text.append(Text.translatable("item.easy_painter.painting.set", EasyPainter.getPaintingDisplayName(current).formatted(Formatting.ITALIC)));
        return text.setStyle(text.getStyle().withItalic(text.getStyle().isItalic()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt().putBoolean("EntityTag", false);
        } else if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt().getBoolean("EntityTag")) {
            NbtCompound entityTag = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt();
            Identifier current = Identifier.tryParse(entityTag.getString("Motive"));
            int newRaw = Registries.PAINTING_VARIANT.getRawId(Registries.PAINTING_VARIANT.get(current)) + 1;
            if (newRaw >= Registries.PAINTING_VARIANT.getIds().size()) {
                newRaw = 0;
            }

            entityTag.putString("Motive", Registries.PAINTING_VARIANT.getId(Registries.PAINTING_VARIANT.get(newRaw)).toString());
        } else {
            NbtCompound entityTag = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt();
            entityTag.putString("Motive", Registries.PAINTING_VARIANT.getId(Registries.PAINTING_VARIANT.get(PaintingVariants.ALBAN.getRegistry())).toString());
        }

        if (!world.isClient) {
            stack.set(DataComponentTypes.CUSTOM_NAME, this.getName(stack));
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            PaintingEntity paintingEntity = new PaintingEntity(world, blockPos2, direction, Registries.PAINTING_VARIANT.getEntry(PaintingVariants.KEBAB.getRegistry()).get());

            NbtComponent nbtCompound = itemStack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtCompound != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, paintingEntity, nbtCompound);
            } else {
                SelectionGui.createGui(paintingEntity, (ServerPlayerEntity) playerEntity).open();
            }

            if (paintingEntity.canStayAttached()) {
                if (!world.isClient) {
                    ((AbstractDecorationEntity)paintingEntity).onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, blockPos);
                    world.spawnEntity(paintingEntity);
                }

                itemStack.decrement(1);
                return ActionResult.success(world.isClient);
            } else {
                assert playerEntity != null;
                playerEntity.sendMessage(Text.translatable("message.easy_painter.painting_cant_fit").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
        }
    }

}
