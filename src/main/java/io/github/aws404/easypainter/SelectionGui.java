package io.github.aws404.easypainter;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.aws404.easypainter.custom.CustomMotivesManager;
import io.github.aws404.easypainter.custom.MotiveCacheState;
import io.github.aws404.easypainter.mixin.AbstractDecorationEntityAccessor;
import io.github.aws404.easypainter.mixin.PaintingEntityMixinAccessor;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectionGui extends SimpleGui {

    private final PaintingEntity entity;

    private SelectionGui(PaintingEntity entity, List<PaintingVariant> motives, ServerPlayerEntity player) {
        super(SelectionGui.getHandlerFromItems(motives.size()), player, false);
        this.entity = entity;
        this.setTitle(Text.translatable("screen.easy_painter.title"));

        for (PaintingVariant possibility : motives) {
            Identifier id = Registries.PAINTING_VARIANT.getId(possibility);
            if (possibility instanceof CustomMotivesManager.CustomMotive) {
                Identifier newId = EasyPainter.customMotivesManager.getMotiveId((CustomMotivesManager.CustomMotive) possibility);
                if (newId != null) {
                    id = newId;
                }
            }

            GuiElementBuilder builder = new GuiElementBuilder(Items.PAINTING)
                    .setName(EasyPainter.getPaintingDisplayName(id).formatted(Formatting.YELLOW))
                    .addLoreLine(Text.literal("")
                            .append(Text.translatable("screen.easy_painter.bullet").formatted(Formatting.GOLD))
                            .append(Text.translatable("screen.easy_painter.size",
                                    Text.translatable("screen.easy_painter.size.data", possibility.getHeight() / 16, possibility.getWidth() / 16).formatted(Formatting.WHITE)
                            ).formatted(Formatting.YELLOW))
                    )
                    .setCallback((index, type1, action) -> {
                        this.close();
                        this.changePainting(possibility);
                    });

            if (possibility == entity.getVariant()) {
                builder.addLoreLine(Text.literal(""));
                builder.addLoreLine(Text.translatable("screen.easy_painter.currently_selected").formatted(Formatting.GRAY));
                builder.glow();
            }

            this.addSlot(builder);
        }
    }

    private void changePainting(PaintingVariant motive) {
        if (motive instanceof CustomMotivesManager.CustomMotive) {
            ((PaintingEntityMixinAccessor) this.entity).setCustomVariant((CustomMotivesManager.CustomMotive) motive);
        } else {
            Optional<RegistryEntry.Reference<PaintingVariant>> variant = Registries.PAINTING_VARIANT.getEntry(Registries.PAINTING_VARIANT.getId(motive));
            if (variant.isEmpty()) return;
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
        List<PaintingVariant> motives1 = Registries.PAINTING_VARIANT.stream().filter(motive -> EasyPainter.canPaintingAttach(entity, motive)).sorted(Comparator.comparingInt(o -> o.getHeight() * o.getWidth())).collect(Collectors.toList());
        List<CustomMotivesManager.CustomMotive> motives2 = new ArrayList<>(EasyPainter.customMotivesManager.getMotives().values());
        List<PaintingVariant> motives = Stream.concat(motives1.stream(), motives2.stream()).toList();
        return new SelectionGui(entity, motives, player);
    }

    private static ScreenHandlerType<?> getHandlerFromItems(int count) {
        return switch ((int) Math.ceil(count / 9.0)) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }

}
