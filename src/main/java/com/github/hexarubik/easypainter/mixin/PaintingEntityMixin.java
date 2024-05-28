package com.github.hexarubik.easypainter.mixin;

import com.github.hexarubik.easypainter.EasyPainter;
import com.github.hexarubik.easypainter.PaintingEntityAccessor;
import com.github.hexarubik.easypainter.SelectionGui;
import com.github.hexarubik.easypainter.custom.CustomFrameEntity;
import com.github.hexarubik.easypainter.custom.CustomMotivesManager;
import com.github.hexarubik.easypainter.custom.MotiveCacheState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(PaintingEntity.class)
public abstract class PaintingEntityMixin extends AbstractDecorationEntity implements PaintingEntityAccessor {
    @Final
    @Shadow
    private static TrackedData<RegistryEntry<PaintingVariant>> VARIANT;
    @Unique
    private boolean locked = false;
    @Unique
    private Set<CustomFrameEntity> customPaintingFrames = new HashSet<>();
    @Unique
    private CustomMotivesManager.CustomMotive cachedMotive;
    @Unique
    @Nullable
    private CustomMotivesManager.CustomMotive CUSTOM_VARIANT;
    @Unique
    private int customFrameEntityLength = 0;
    protected PaintingEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);

    }

    @Shadow
    public abstract int getWidthPixels();

    @Shadow
    public abstract int getHeightPixels();

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.locked && player.isSneaking()) {
            SelectionGui.createGui((PaintingEntity) (Object) this, (ServerPlayerEntity) player).open();
        }
        return super.interact(player, hand);
    }

    @Override
    public boolean canStayAttached() {
        if (!this.locked) {
            PaintingVariant variant = this.dataTracker.get(VARIANT).value();
            EasyPainter.canPaintingAttach((PaintingEntity) (Object) this, variant);
        }
        return true;
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        if (this.locked) {
            return false;
        }
        return super.handleAttack(attacker);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.locked) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public void tick() {
        if (CUSTOM_VARIANT != this.cachedMotive) {
            for (CustomFrameEntity customPaintingFrame : this.customPaintingFrames) {
                customPaintingFrame.remove(RemovalReason.DISCARDED);
            }
            this.customPaintingFrames.clear();
        }

        if (CUSTOM_VARIANT != null && ((CUSTOM_VARIANT != this.cachedMotive) || (this.customPaintingFrames.size() != this.customFrameEntityLength))) {
            MotiveCacheState.Entry state = CUSTOM_VARIANT.state;
            this.customPaintingFrames.clear();
            this.customFrameEntityLength = state.blockWidth * state.blockHeight;

            int widthBlocks = CUSTOM_VARIANT.getWidth() / 16;
            int heightBlocks = CUSTOM_VARIANT.getHeight() / 16;

            int attachX = (widthBlocks - 1) / -2;
            int attachY = (heightBlocks - 1) / -2;

            Direction rotated = this.facing.rotateYCounterclockwise();

            int i = 0;
            for (int x = 0; x < widthBlocks; x++) {
                for (int y = 0; y < heightBlocks; y++) {
                    BlockPos pos = new BlockPos.Mutable().set(this.attachmentPos).move(rotated, x + attachX).move(Direction.UP, (heightBlocks - y) + attachY - 1);

                    ItemStack stack = CUSTOM_VARIANT.createMapItem(x, y);


                    CustomFrameEntity entity = new CustomFrameEntity(this.getWorld(), (PaintingEntity) (Object) this, pos, stack, i);
                    this.getWorld().spawnEntity(entity);

                    this.customPaintingFrames.add(entity);

                    i++;
                }
            }
        }

        this.cachedMotive = CUSTOM_VARIANT;
        super.tick();
    }

    public CustomMotivesManager.CustomMotive easy_painter_master$getCustomVariant() {
        return CUSTOM_VARIANT;
    }

    public void easy_painter_master$setCustomVariant(CustomMotivesManager.CustomMotive motive) {
        CUSTOM_VARIANT = motive;
    }

	/*public void easy_painter_master$addCustomPaintingFrame(CustomFrameEntity entity, int i) {
		//this.customPaintingFrames[i] = entity;
	}*/

    public boolean easy_painter_master$isEntityInCustonPaintingFrameList(CustomFrameEntity entity) {
        return this.customPaintingFrames.contains(entity);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.locked = nbt.contains("Locked") && nbt.getBoolean("Locked");
        if (nbt.contains("Length")) {
            this.customFrameEntityLength = nbt.getInt("Length");
        }
        if (nbt.contains("CustomVariant")) {
            Identifier id = new Identifier(nbt.getString("CustomVariant"));
            CUSTOM_VARIANT = EasyPainter.customMotivesManager.getMotive(id);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("Locked", this.locked);
        nbt.putInt("Length", this.customFrameEntityLength);
        if (CUSTOM_VARIANT != null) {
            nbt.putString("CustomVariant", CUSTOM_VARIANT.state.getId().toString());
        }
    }

    @Inject(method = "createSpawnPacket", at = @At("HEAD"), cancellable = true)
    private void createSpawnPacket(CallbackInfoReturnable<Packet<?>> cir) {
        if (CUSTOM_VARIANT != null) {
            cir.setReturnValue(new EntitiesDestroyS2CPacket(this.getId()));
        }
    }

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void onBreak(Entity entity, CallbackInfo ci) {
        for (CustomFrameEntity customPaintingFrame : this.customPaintingFrames) {
            customPaintingFrame.remove(RemovalReason.DISCARDED);
        }
    }
}
