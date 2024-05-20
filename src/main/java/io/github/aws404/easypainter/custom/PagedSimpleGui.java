package io.github.aws404.easypainter.custom;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.aws404.easypainter.EasyPainter;
import io.github.aws404.easypainter.SelectionGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PagedSimpleGui extends SimpleGui {
    private final List<GuiElementInterface> slots;
    private int page = 1;

    private final int maxItemsPerPage = 9 * 5;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     * @param manipulatePlayerSlots if <code>true</code> the players inventory
     *                              will be treated as slots of this gui
     */
    public PagedSimpleGui(ServerPlayerEntity player, boolean manipulatePlayerSlots) {
        super(ScreenHandlerType.GENERIC_9X6, player, manipulatePlayerSlots);

        this.slots = new ArrayList<>();

        GuiElementBuilder builder = new GuiElementBuilder(Items.MAGENTA_GLAZED_TERRACOTTA)
                .setName(Text.translatable("screen.easy_painter.previous_page").formatted(Formatting.YELLOW))
                .setCallback((index, type1, action) -> {
                    this.page = (int) Math.clamp(this.page - 1, 1, Math.ceil(this.slots.size() / maxItemsPerPage));
                    updatePage();
                });
        this.setSlot(45, builder);

        GuiElementBuilder builder2 = new GuiElementBuilder(Items.MAGENTA_GLAZED_TERRACOTTA)
                .setName(Text.translatable("screen.easy_painter.next_page").formatted(Formatting.YELLOW))
                .setCallback((index, type1, action) -> {
                    this.page = (int) Math.clamp(this.page + 1, 1, Math.ceil(this.slots.size() / maxItemsPerPage));
                    updatePage();
                });
        this.setSlot(53, builder2);

        updatePage();
    }

    private void updatePage() {
        for (int i = 0; i < maxItemsPerPage; i++) {
            this.clearSlot(i);
        }

        System.out.println(this.page);
        int startIndex = this.page * maxItemsPerPage;
        for (int i = startIndex; i < startIndex + maxItemsPerPage; i++) {
            if (i >= this.slots.size()) break;
            GuiElementInterface slot = this.slots.get(i);
           // if (slot == null) break;
            this.setSlot(this.getFirstEmptySlot(), slot);
        }
    }

    @Override
    public void addSlot(GuiElementBuilderInterface<?> element) {
        int slotIndex = this.getFirstEmptySlot();

        int height = this.getHeight(); System.out.println((Math.ceil((double) slotIndex / 9)));
        if (Math.ceil((double) slotIndex / 9) == height) return;;

        this.slots.add(element.build());
        updatePage();
        //super.addSlot(element);
    }
}
