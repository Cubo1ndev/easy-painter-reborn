package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.custom.CustomMotivesManager;

public interface PaintingEntityMixinAccessor {
    CustomMotivesManager.CustomMotive getCustomVariant();

    void setCustomVariant(CustomMotivesManager.CustomMotive motive);
}
