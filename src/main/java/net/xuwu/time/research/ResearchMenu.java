package net.xuwu.time.research;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.xuwu.time.registry.TimeContent;

public final class ResearchMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    public ResearchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, resolve(inventory, buffer), new SimpleContainerData(2));
    }
    private static Container resolve(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        // Generic MenuProvider callers need no block payload; slot/data sync still works.
        if (buffer == null || buffer.readableBytes() < Long.BYTES) return new SimpleContainer(4);
        var be = inventory.player.level().getBlockEntity(buffer.readBlockPos());
        return be instanceof ResearchDeskBlockEntity desk ? desk : new SimpleContainer(4);
    }
    public ResearchMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(TimeContent.RESEARCH_MENU.get(), id); this.container = container; this.data = data;
        checkContainerSize(container, 4); checkContainerDataCount(data, 2);
        addSlot(new Slot(container, 0, 14, 35)); addSlot(new Slot(container, 1, 50, 35)); addSlot(new Slot(container, 2, 86, 35));
        addSlot(new Slot(container, 3, 144, 35) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    public int progressPixels(int width) { return data.get(1) <= 0 ? 0 : Math.min(width, data.get(0) * width / data.get(1)); }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 3, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
