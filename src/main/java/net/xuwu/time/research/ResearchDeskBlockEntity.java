package net.xuwu.time.research;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.xuwu.time.registry.TimeContent;

public final class ResearchDeskBlockEntity extends BlockEntity implements Container, MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int progress, duration = 400;
    private String activeRecipe = "";
    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? progress : duration; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; else duration = value; }
        @Override public int getCount() { return 2; }
    };
    public ResearchDeskBlockEntity(BlockPos pos, BlockState state) { super(TimeContent.RESEARCH_BE.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, ResearchDeskBlockEntity desk) {
        var input = new ResearchRecipe.Input(desk.items.get(0), desk.items.get(1), desk.items.get(2));
        var match = level.getRecipeManager().getRecipeFor(TimeContent.RESEARCH_TYPE.get(), input, level);
        if (match.isEmpty()) {
            if (desk.progress != 0) { desk.progress = 0; desk.activeRecipe = ""; desk.setChanged(); }
            return;
        }
        var holder = match.get();
        var recipe = holder.value();
        if (!desk.activeRecipe.equals(holder.id().toString())) { desk.progress = 0; desk.activeRecipe = holder.id().toString(); }
        desk.duration = recipe.duration();
        ItemStack output = recipe.assemble(input, level.registryAccess()), existing = desk.items.get(3);
        if (!existing.isEmpty() && (!ItemStack.isSameItemSameComponents(existing, output) || existing.getCount() + output.getCount() > existing.getMaxStackSize())) return;
        if (++desk.progress >= desk.duration) {
            if (recipe.consumeEvidence()) desk.consume(0);
            desk.consume(1);
            if (recipe.consumeReference()) desk.consume(2);
            if (existing.isEmpty()) desk.items.set(3, output); else existing.grow(output.getCount());
            desk.progress = 0;
        }
        desk.setChanged();
    }
    private void consume(int slot) {
        var stack = items.get(slot);
        var remainder = stack.getCraftingRemainingItem();
        stack.shrink(1);
        if (!remainder.isEmpty()) {
            if (stack.isEmpty()) items.set(slot, remainder);
            else if (level != null) Containers.dropItemStack(level, worldPosition.getX() + .5, worldPosition.getY() + 1, worldPosition.getZ() + .5, remainder);
        }
    }
    @Override public int getContainerSize() { return 4; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        var result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(getMaxStackSize(stack)); setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot != 3; }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public Component getDisplayName() { return Component.translatable("block.time.research_desk"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new ResearchMenu(id, inventory, this, data); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup); ContainerHelper.saveAllItems(tag, items, lookup);
        tag.putInt("Progress", progress); tag.putInt("Duration", duration); tag.putString("Recipe", activeRecipe);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup); ContainerHelper.loadAllItems(tag, items, lookup);
        progress = Math.max(0, tag.getInt("Progress")); duration = Math.max(1, tag.getInt("Duration")); activeRecipe = tag.getString("Recipe");
    }
}
