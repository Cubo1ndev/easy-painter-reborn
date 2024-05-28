package com.github.hexarubik.easypainter;

import com.github.hexarubik.easypainter.custom.CustomFrameEntity;
import com.github.hexarubik.easypainter.custom.CustomMotivesManager;

public interface PaintingEntityAccessor {
    CustomMotivesManager.CustomMotive easy_painter_master$getCustomVariant();

    void easy_painter_master$setCustomVariant(CustomMotivesManager.CustomMotive motive);

    //void easy_painter_master$addCustomPaintingFrame(CustomFrameEntity entity, int i);

    boolean easy_painter_master$isEntityInCustonPaintingFrameList(CustomFrameEntity entity);
}
