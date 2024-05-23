package io.github.aws404.easypainter;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import io.github.aws404.easypainter.custom.CustomMotivesManager;
import io.github.aws404.easypainter.custom.PagedSimpleGui;
import io.github.aws404.easypainter.mixin.AbstractDecorationEntityAccessor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Stream;

public class SelectionGui extends PagedSimpleGui {

    private final PaintingEntity entity;

    private SelectionGui(PaintingEntity entity, List<PaintingVariant> motives, ServerPlayerEntity player) {
        super(player, false);
        this.entity = entity;
        this.setTitle(Text.translatable("screen.easy_painter.title"));

        ArrayList<GuiElementBuilder> elements = new ArrayList<>();
        for (PaintingVariant possibility : motives) {
            Identifier id = Registries.PAINTING_VARIANT.getId(possibility);
            CustomModelDataComponent customModelData = CustomModelDataComponent.DEFAULT;
            boolean isSelected;
            if (possibility instanceof CustomMotivesManager.CustomMotive) {
                Identifier newId = EasyPainter.customMotivesManager.getMotiveId((CustomMotivesManager.CustomMotive) possibility);
                if (newId != null) {
                    id = newId;
                }

                customModelData = new CustomModelDataComponent(((CustomMotivesManager.CustomMotive) possibility).state.customModelData);
            }

            CustomMotivesManager.CustomMotive motive = ((PaintingEntityAccessor) entity).easy_painter_master$getCustomVariant();
            if (motive != null) {
                isSelected = motive == possibility;
            } else {
                isSelected = entity.getVariant().value() == possibility;
            }

            GuiElementBuilder builder = new GuiElementBuilder(Items.PAINTING)
                    .setName(EasyPainter.getPaintingDisplayName(id).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("")
                            .append(EasyPainter.getPaintingAuthor(id).formatted(Formatting.GRAY))
                    )
                    .addLoreLine(Text.literal(""))
                    .addLoreLine(Text.literal("")
                            .append(Text.translatable("screen.easy_painter.size",
                                    Text.translatable("screen.easy_painter.size.data", possibility.getHeight() / 16, possibility.getWidth() / 16)
                            ).formatted(Formatting.GRAY))
                    )
                    .setCallback((index, type1, action) -> {
                        this.close();
                        this.changePainting(possibility);
                    });
            builder.setComponent(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

            if (isSelected) {
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.translatable("screen.easy_painter.currently_selected").formatted(Formatting.GRAY));
                builder.glow();
            }

            elements.add(builder);
        }

        this.addSlots(elements);
    }


    private void changePainting(PaintingVariant motive) {
        if (motive instanceof CustomMotivesManager.CustomMotive) {
            ((PaintingEntityAccessor) this.entity).easy_painter_master$setCustomVariant((CustomMotivesManager.CustomMotive) motive);
        } else {
            Optional<RegistryEntry.Reference<PaintingVariant>> variant = Registries.PAINTING_VARIANT.getEntry(Registries.PAINTING_VARIANT.getId(motive));
            if (variant.isEmpty()) return;
            ((PaintingEntityAccessor) this.entity).easy_painter_master$setCustomVariant(null);
            this.entity.setVariant(variant.get());
        }

        ((AbstractDecorationEntityAccessor) this.entity).callUpdateAttachmentPosition();
        Objects.requireNonNull(this.entity.getServer()).getPlayerManager().sendToAll(new EntitiesDestroyS2CPacket(this.entity.getId()));
        Packet<?> packet = this.entity.createSpawnPacket();
        if (packet != null) {
            this.entity.getServer().getPlayerManager().sendToAll(packet);
        }
    }

    public static SelectionGui createGui(PaintingEntity entity, ServerPlayerEntity player) {
        List<PaintingVariant> motives1 = Registries.PAINTING_VARIANT.stream().filter(motive -> EasyPainter.canPaintingAttach(entity, motive)).sorted(Comparator.comparingInt(o -> o.getHeight() * o.getWidth())).toList();
        List<CustomMotivesManager.CustomMotive> motives2 = EasyPainter.customMotivesManager.getMotives().values().stream().filter(motive -> EasyPainter.canPaintingAttach(entity, motive)).sorted(Comparator.comparingInt(o -> o.getHeight() * o.getWidth())).toList();
        List<PaintingVariant> motives = Stream.concat(motives1.stream(), motives2.stream()).toList();
        return new SelectionGui(entity, motives, player);
    }
}
