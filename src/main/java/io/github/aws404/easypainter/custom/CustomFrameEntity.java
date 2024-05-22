package io.github.aws404.easypainter.custom;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import io.github.aws404.easypainter.EasyPainter;
import io.github.aws404.easypainter.PaintingEntityAccessor;
import io.github.aws404.easypainter.mixin.ItemFrameEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * This is the extension of ItemFrame used for custom painting motives
 */
public class CustomFrameEntity extends ItemFrameEntity implements PolymerEntity {

    public PaintingEntity painting;
    public CustomMotivesManager.CustomMotive motive;
    public int index;

    public CustomFrameEntity(World world, PaintingEntity painting, BlockPos pos, ItemStack stack, int i) {
        super(EasyPainter.CUSTOM_ITEM_FRAME_ENTITY_TYPE, world, pos, painting.getHorizontalFacing());
        this.painting = painting;
        this.motive = ((PaintingEntityAccessor) this.painting).easy_painter_master$getCustomVariant();
        this.index = i;
        ((ItemFrameEntityAccessor) this).setFixed(true);
        this.setHeldItemStack(stack);
    }

    public CustomFrameEntity(EntityType<CustomFrameEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public int getWidthPixels() {
        return 16;
    }

    @Override
    public int getHeightPixels() {
        return 16;
    }

    @Override
    public void tick() {
        if (this.painting.isRemoved()) {
            this.remove(RemovalReason.DISCARDED);
        }
        this.updateAttachmentPosition();
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
       /* if (nbt.contains("PaintingId") && nbt.contains("PaintingIndex")) {
            this.painting = (PaintingEntity) this.getWorld().getEntityById(nbt.getInt("PaintingId"));
            this.index = nbt.getInt("PaintingIndex");
            System.out.println(this.painting);
            System.out.println(this.index);
            ((PaintingEntityAccessor) this.painting).easy_painter_master$addCustomPaintingFrame(this, this.index);
        } else {
            this.kill();
        }*/
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("PaintingId", this.painting.getId());
        nbt.putInt("PaintingIndex", this.index);

        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void onBreak(@Nullable Entity entity) {
        this.painting.onBreak(entity);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return this.painting.damage(source, amount);
    }

    @Override
    public void onPlace() {
        this.painting.onPlace();
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return this.painting.handleAttack(attacker);
    }

    @Override
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        return null;
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        if (((PaintingEntityAccessor) this.painting).easy_painter_master$getCustomVariant() != null) {
            return new EntitySpawnS2CPacket(this, this.facing.getId(), this.getDecorationBlockPos());
        }
        return new EntitiesDestroyS2CPacket(this.getId());
    }

    @Override
    public boolean canStayAttached() {
        return true;
    }

    @Override
    public void kill() {
        this.painting.kill();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        return this.painting.interact(player, hand);
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.ITEM_FRAME;
    }
}
