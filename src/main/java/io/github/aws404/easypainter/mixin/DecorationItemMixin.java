package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.EasyPainter;
import io.github.aws404.easypainter.SelectionGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.decoration.painting.PaintingVariants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;
import java.util.Optional;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin extends Item {

    //@Shadow protected abstract void canPlaceOn();

    public DecorationItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        MutableText text = (MutableText) super.getName(stack);
        NbtComponent entityComponent = stack.get(DataComponentTypes.ENTITY_DATA);
        String str = entityComponent.copyNbt().getString("custom_variant");
        Identifier current = Identifier.tryParse(str);
        text.append(Text.translatable("item.easy_painter.painting.set", EasyPainter.getPaintingDisplayName(current).formatted(Formatting.ITALIC)));
        return text.setStyle(text.getStyle().withItalic(text.getStyle().isItalic()));
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            System.out.println("1");
            Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt().putBoolean("EntityTag", false);
        } else if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt().getBoolean("EntityTag")) {
            System.out.println("2");
            NbtComponent nbtComponent = stack.get(DataComponentTypes.ENTITY_DATA);
            Identifier current = Identifier.tryParse(nbtComponent.copyNbt().getString("Motive"));
            int newRaw = Registries.PAINTING_VARIANT.getRawId(Registries.PAINTING_VARIANT.get(current)) + 1;
            if (newRaw >= Registries.PAINTING_VARIANT.getIds().size()) {
                newRaw = 0;
            }
            nbtComponent.getNbt().putString("Motive", Registries.PAINTING_VARIANT.getId(Registries.PAINTING_VARIANT.get(newRaw)).toString());
            stack.set(DataComponentTypes.ENTITY_DATA, nbtComponent);
        } else {
            System.out.println("3");
            //NbtCompound entityTag = Objects.requireNonNull(stack.get(DataComponentTypes.CUSTOM_DATA)).getNbt();
            //entityTag.putString("Motive", Registries.PAINTING_VARIANT.getId(Registries.PAINTING_VARIANT.get(PaintingVariants.ALBAN.getRegistry())).toString());
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
                    ((AbstractDecorationEntity)paintingEntity).onPlace();
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
