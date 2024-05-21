package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.custom.CustomMotivesManager;

//@Mixin(PaintingEntityMixin.class)
public interface PaintingEntityMixinAccessor {
    //@Accessor
    CustomMotivesManager.CustomMotive easy_painter_master$getCustomVariant();

    //@Accessor
    void easy_painter_master$setCustomVariant(CustomMotivesManager.CustomMotive motive);
}
