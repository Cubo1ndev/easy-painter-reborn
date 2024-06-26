package io.github.aws404.easypainter.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.SERVER)
@Mixin(MapState.class)
public interface MapStateAccessor {
    @Accessor("locked")
    @Mutable
    void setLocked(boolean locked);
}
