package com.github.hexarubik.easypainter.custom;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PagedSimpleGui extends SimpleGui {
    private final List<GuiElementInterface> slots;
    private final int maxItemsPerPage = 9 * 5;
    private final GuiElementInterface previousPage;
    private final GuiElementInterface nextPage;
    private int page = 0;

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

        GuiElementBuilder builder = new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setComponent(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1))
                .setName(Text.translatable("screen.easy_painter.previous_page").formatted(Formatting.YELLOW))
                .setCallback((index, type1, action) -> {
                    this.page = Math.clamp(this.page - 1, 0, this.getMaxPages());
                    this.updatePage();
                });
        this.setSlot(48, builder);

        GuiElementBuilder builder2 = new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setComponent(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(2))
                .setName(Text.translatable("screen.easy_painter.page_1").formatted(Formatting.YELLOW));
        this.setSlot(49, builder2);

        GuiElementBuilder builder3 = new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setComponent(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(3))
                .setName(Text.translatable("screen.easy_painter.next_page").formatted(Formatting.YELLOW))
                .setCallback((index, type1, action) -> {
                    this.page = Math.clamp(this.page + 1, 0, this.getMaxPages());
                    this.updatePage();
                });
        this.setSlot(50, builder3);

        previousPage = this.getSlot(48);
        nextPage = this.getSlot(50);

        this.updatePage();
    }

    private int getMaxPages() {
        return (int) Math.ceil((double) this.slots.size() / maxItemsPerPage);
    }

    private void updatePage() {
        for (int i = 0; i < maxItemsPerPage; i++) {
            this.clearSlot(i);
        }

        int startIndex = this.page * maxItemsPerPage;
        for (int i = startIndex; i < startIndex + maxItemsPerPage; i++) {
            if (i >= this.slots.size()) break;
            GuiElementInterface slot = this.slots.get(i);
            this.setSlot(this.getFirstEmptySlot(), slot);
        }

        GuiElementInterface element = this.getSlot(49);
        if (element != null) {
            int realPage = this.page + 1;
            element.getItemStack().set(DataComponentTypes.ITEM_NAME, Text.translatable("screen.easy_painter.page_" + (realPage)).formatted(Formatting.YELLOW));
        }

        int maxPages = this.getMaxPages() - 1;
        if (maxPages != 0) {
            if (this.page == 0) {
                this.clearSlot(48);
                this.setSlot(50, nextPage);
            } else if (this.page == maxPages) {
                this.setSlot(48, previousPage);
                this.clearSlot(50);
            } else {
                this.setSlot(48, previousPage);
                this.setSlot(50, nextPage);
            }
        } else {
            this.clearSlot(48);
            this.clearSlot(50);
        }
    }

    @Override
    public void addSlot(GuiElementBuilderInterface<?> element) {
        this.slots.add(element.build());
    }

    public void addSlots(ArrayList<GuiElementBuilder> elements) {
        for (GuiElementBuilder element : elements) {
            this.addSlot(element);
        }
        this.updatePage();
    }
}
