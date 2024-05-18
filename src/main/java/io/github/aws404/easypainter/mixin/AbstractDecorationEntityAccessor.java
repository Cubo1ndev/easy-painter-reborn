package io.github.aws404.easypainter.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.SERVER)
@Mixin(AbstractDecorationEntity.class)
public interface AbstractDecorationEntityAccessor {
    @Invoker
    void callUpdateAttachmentPosition();
}
