package net.xuwu.time.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.xuwu.time.research.ResearchMenu;

public final class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {
    private static final ResourceLocation PANEL = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation PROGRESS = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

    public ResearchScreen(ResearchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176; imageHeight = 166;
        inventoryLabelX = 8; inventoryLabelY = 72;
    }
    @Override protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        // Reuse vanilla pixels instead of shipping a recoloured copy of the UI.
        g.blit(PANEL, x, y, 0, 0, imageWidth, imageHeight);
        for (int row = 16; row < 72; row += 8) {
            g.blit(PANEL, x + 4, y + row, 4, 4, 168, 8);
        }
        for (int i = 0; i < 3; i++) {
            var slot = menu.getSlot(i);
            g.blit(PANEL, x + slot.x - 1, y + slot.y - 1, 55, 16, 18, 18);
        }
        var output = menu.getSlot(3);
        g.blit(PANEL, x + output.x - 5, y + output.y - 5, 111, 30, 26, 26);
        g.blit(PANEL, x + 110, y + 35, 79, 34, 24, 16);
        g.blitSprite(PROGRESS, 24, 16, 0, 0, x + 110, y + 35, menu.progressPixels(24), 16);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        String[] labels = {"evidence", "catalyst", "reference", "output"};
        for (int i = 0; i < labels.length; i++) {
            Component text = Component.translatable("menu.time." + labels[i]);
            g.drawString(font, text, menu.getSlot(i).x + 8 - font.width(text) / 2, 21, 0x404040, false);
        }
        g.drawString(font, Component.translatable("menu.time.research_hint"), 8, 60, 0x606060, false);
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
        if (isHovering(8, 58, 160, 12, mouseX, mouseY)) {
            g.renderTooltip(font, Component.translatable("menu.time.note"), mouseX, mouseY);
        }
    }
}
