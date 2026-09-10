package net.xuwu.time.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.xuwu.time.TimeMod;
import net.xuwu.time.registry.TimeContent;

public record ResearchRecipe(Ingredient evidence, Ingredient catalyst, Ingredient reference, ItemStack result,
                             int duration, boolean consumeEvidence, boolean consumeReference) implements Recipe<ResearchRecipe.Input> {
    public record Input(ItemStack evidence, ItemStack catalyst, ItemStack reference) implements Container {
        @Override public ItemStack getItem(int index) {
            return switch (index) { case 0 -> evidence; case 1 -> catalyst; case 2 -> reference; default -> throw new IndexOutOfBoundsException(index); };
        }
        @Override public int getContainerSize() { return 3; }
        @Override public boolean isEmpty() { return evidence.isEmpty() && catalyst.isEmpty() && reference.isEmpty(); }
        @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
        @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
        @Override public void setItem(int slot, ItemStack stack) {}
        @Override public void setChanged() {}
        @Override public boolean stillValid(Player player) { return true; }
        @Override public void clearContent() {}
    }
    @Override public boolean matches(Input input, Level level) {
        return evidence.test(input.evidence) && catalyst.test(input.catalyst) && reference.test(input.reference);
    }
    @Override public ItemStack assemble(Input input, RegistryAccess registries) { return result.copy(); }
    @Override public ItemStack getResultItem(RegistryAccess registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 3; }
    @Override public RecipeSerializer<?> getSerializer() { return TimeContent.RESEARCH_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return TimeContent.RESEARCH_TYPE.get(); }
    @Override public ResourceLocation getId() { return TimeMod.id("research"); }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, evidence, catalyst, reference); }
    @Override public ItemStack getToastSymbol() { return new ItemStack(TimeContent.RESEARCH_DESK.get()); }
    @Override public boolean isSpecial() { return true; }

    public static final class Serializer implements RecipeSerializer<ResearchRecipe> {
        @Override public ResearchRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient evidence = Ingredient.fromJson(json.get("evidence"));
            Ingredient catalyst = Ingredient.fromJson(json.get("catalyst"));
            Ingredient reference = Ingredient.fromJson(json.get("reference"));
            ItemStack result = readResult(json.getAsJsonObject("result"));
            int duration = json.has("duration") ? Math.max(1, Math.min(72000, json.get("duration").getAsInt())) : 400;
            boolean consumeEvidence = json.has("consume_evidence") && json.get("consume_evidence").getAsBoolean();
            boolean consumeReference = json.has("consume_reference") && json.get("consume_reference").getAsBoolean();
            return new ResearchRecipe(evidence, catalyst, reference, result, duration, consumeEvidence, consumeReference);
        }
        private static ItemStack readResult(JsonObject json) {
            String name = json.has("item") ? json.get("item").getAsString() : json.get("id").getAsString();
            ResourceLocation id = ResourceLocation.tryParse(name);
            Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
            if (item == Items.AIR) throw new JsonParseException("Unknown research result item: " + name);
            return new ItemStack(item, json.has("count") ? json.get("count").getAsInt() : 1);
        }
        @Override public void toNetwork(FriendlyByteBuf buf, ResearchRecipe recipe) {
            recipe.evidence.toNetwork(buf); recipe.catalyst.toNetwork(buf); recipe.reference.toNetwork(buf);
            buf.writeItem(recipe.result); buf.writeVarInt(recipe.duration); buf.writeBoolean(recipe.consumeEvidence); buf.writeBoolean(recipe.consumeReference);
        }
        @Override public ResearchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new ResearchRecipe(Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf), Ingredient.fromNetwork(buf),
                buf.readItem(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
        }
    }
}
