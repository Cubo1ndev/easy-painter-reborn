package io.github.aws404.easypainter;

import io.github.aws404.easypainter.custom.CustomFrameEntity;
import io.github.aws404.easypainter.custom.CustomMotivesManager;

public interface PaintingEntityAccessor {
    CustomMotivesManager.CustomMotive easy_painter_master$getCustomVariant();

    void easy_painter_master$setCustomVariant(CustomMotivesManager.CustomMotive motive);

    //void easy_painter_master$addCustomPaintingFrame(CustomFrameEntity entity, int i);

    boolean easy_painter_master$isEntityInCustonPaintingFrameList(CustomFrameEntity entity);
}
