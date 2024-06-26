package com.github.hexarubik.easypainter.mixin;

import com.github.hexarubik.easypainter.EasyPainter;
import com.github.hexarubik.easypainter.SelectionGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.Optional;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin extends Item {

    @Shadow
    @Final
    private EntityType<? extends AbstractDecorationEntity> entityType;

    public DecorationItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        if (this.entityType != EntityType.PAINTING) return this.getName();

        MutableText text = (MutableText) super.getName(stack);
        NbtComponent entityComponent = stack.get(DataComponentTypes.ENTITY_DATA);
        if (entityComponent == null) return this.getName();

        String str = entityComponent.copyNbt().getString("custom_variant");
        Identifier current = Identifier.tryParse(str);
        text.append(Text.translatable("item.easy_painter.painting.set", EasyPainter.getPaintingDisplayName(current).formatted(Formatting.ITALIC)));
        return text.setStyle(text.getStyle().withItalic(text.getStyle().isItalic()));
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (this.entityType != EntityType.PAINTING) return TypedActionResult.success(stack);

        if (user.isSneaking()) {
            Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt().putBoolean("EntityTag", false);
        } else if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt().getBoolean("EntityTag")) {
            NbtComponent nbtComponent = stack.get(DataComponentTypes.ENTITY_DATA);
            if (nbtComponent != null) {
                Identifier current = Identifier.tryParse(nbtComponent.copyNbt().getString("Motive"));
                int newRaw = Registries.PAINTING_VARIANT.getRawId(Registries.PAINTING_VARIANT.get(current)) + 1;
                if (newRaw >= Registries.PAINTING_VARIANT.getIds().size()) {
                    newRaw = 0;
                }
                nbtComponent.getNbt().putString("Motive", Registries.PAINTING_VARIANT.getId(Registries.PAINTING_VARIANT.get(newRaw)).toString());
                stack.set(DataComponentTypes.ENTITY_DATA, nbtComponent);
            }
        }

        if (!world.isClient) {
            stack.set(DataComponentTypes.CUSTOM_NAME, this.getName(stack));
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (this.entityType != EntityType.PAINTING) {
            AbstractDecorationEntity abstractDecorationEntity;
            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockPos blockPos2 = blockPos.offset(direction);
            PlayerEntity playerEntity = context.getPlayer();
            ItemStack itemStack = context.getStack();
            if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
                return ActionResult.FAIL;
            }
            World world = context.getWorld();
            if (this.entityType == EntityType.PAINTING) {
                Optional<PaintingEntity> optional = PaintingEntity.placePainting(world, blockPos2, direction);
                if (optional.isEmpty()) {
                    return ActionResult.CONSUME;
                }
                abstractDecorationEntity = optional.get();
            } else if (this.entityType == EntityType.ITEM_FRAME) {
                abstractDecorationEntity = new ItemFrameEntity(world, blockPos2, direction);
            } else if (this.entityType == EntityType.GLOW_ITEM_FRAME) {
                abstractDecorationEntity = new GlowItemFrameEntity(world, blockPos2, direction);
            } else {
                return ActionResult.success(world.isClient);
            }
            NbtComponent nbtComponent = itemStack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
            if (!nbtComponent.isEmpty()) {
                EntityType.loadFromEntityNbt(world, playerEntity, abstractDecorationEntity, nbtComponent);
            }
            if (abstractDecorationEntity.canStayAttached()) {
                if (!world.isClient) {
                    abstractDecorationEntity.onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, abstractDecorationEntity.getPos());
                    world.spawnEntity(abstractDecorationEntity);
                }
                itemStack.decrement(1);
                return ActionResult.success(world.isClient);
            }
            return ActionResult.CONSUME;
        }

        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity == null || !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            Optional<RegistryEntry.Reference<PaintingVariant>> optional = Registries.PAINTING_VARIANT.getEntry(Registries.PAINTING_VARIANT.getDefaultId());
            if (optional.isEmpty()) return ActionResult.FAIL;
            PaintingEntity paintingEntity = new PaintingEntity(world, blockPos2, direction, optional.get());

            NbtComponent nbtCompound = itemStack.get(DataComponentTypes.ENTITY_DATA);
            if (nbtCompound != null && nbtCompound.copyNbt().getString("custom_variant") != null) {
                EntityType.loadFromEntityNbt(world, playerEntity, paintingEntity, nbtCompound);
            } else {
                SelectionGui.createGui(paintingEntity, (ServerPlayerEntity) playerEntity).open();
            }

            if (paintingEntity.canStayAttached()) {
                if (!world.isClient) {
                    ((AbstractDecorationEntity) paintingEntity).onPlace();
                    world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, blockPos);
                    world.spawnEntity(paintingEntity);
                }

                itemStack.decrement(1);
                return ActionResult.success(world.isClient);
            } else {
                playerEntity.sendMessage(Text.translatable("message.easy_painter.painting_cant_fit").formatted(Formatting.RED), true);
                return ActionResult.FAIL;
            }
        }
    }

    @Unique
    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }
}
