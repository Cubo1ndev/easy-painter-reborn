package io.github.aws404.easypainter.custom;

import io.github.aws404.easypainter.EasyPainter;
import io.github.aws404.easypainter.mixin.PaintingEntityMixin;
import io.github.aws404.easypainter.mixin.PaintingEntityMixinAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * This is the extension of ItemFrame used for custom painting motives
 */
public class CustomFrameEntity extends ItemFrameEntity {

    public PaintingEntity painting;
    public CustomMotivesManager.CustomMotive motive;

    public CustomFrameEntity(World world, PaintingEntity painting, BlockPos pos, ItemStack stack) {
        super(EntityType.ITEM_FRAME, world, pos, painting.getHorizontalFacing());
        this.painting = painting;
        this.motive = ((PaintingEntityMixinAccessor) this.painting).getCustomVariant();
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
        if (((PaintingEntityMixinAccessor) this.painting).getCustomVariant() != null) {
            System.out.println(1);
            return new EntitySpawnS2CPacket(((ItemFrameEntity)this), 0, this.getDecorationBlockPos());
        }
        System.out.println(2);
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
}
