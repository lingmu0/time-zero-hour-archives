package net.xuwu.time.research;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.xuwu.time.registry.TimeContent;

public record ResearchRecipe(Ingredient evidence, Ingredient catalyst, Ingredient reference, ItemStack result,
                             int duration, boolean consumeEvidence, boolean consumeReference) implements Recipe<ResearchRecipe.Input> {
    public record Input(ItemStack evidence, ItemStack catalyst, ItemStack reference) implements RecipeInput {
        @Override public ItemStack getItem(int index) {
            return switch (index) { case 0 -> evidence; case 1 -> catalyst; case 2 -> reference; default -> throw new IndexOutOfBoundsException(index); };
        }
        @Override public int size() { return 3; }
    }
    @Override public boolean matches(Input input, Level level) {
        return evidence.test(input.evidence) && catalyst.test(input.catalyst) && reference.test(input.reference);
    }
    @Override public ItemStack assemble(Input input, HolderLookup.Provider registries) { return result.copy(); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 3; }
    @Override public RecipeSerializer<?> getSerializer() { return TimeContent.RESEARCH_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return TimeContent.RESEARCH_TYPE.get(); }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, evidence, catalyst, reference); }
    @Override public ItemStack getToastSymbol() { return new ItemStack(TimeContent.RESEARCH_DESK.get()); }
    // This workstation has its own menu; do not add recipes to vanilla's crafting book.
    @Override public boolean isSpecial() { return true; }

    public static final class Serializer implements RecipeSerializer<ResearchRecipe> {
        private static final MapCodec<ResearchRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("evidence").forGetter(ResearchRecipe::evidence),
            Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(ResearchRecipe::catalyst),
            Ingredient.CODEC_NONEMPTY.fieldOf("reference").forGetter(ResearchRecipe::reference),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ResearchRecipe::result),
            Codec.intRange(1, 72000).optionalFieldOf("duration", 400).forGetter(ResearchRecipe::duration),
            Codec.BOOL.optionalFieldOf("consume_evidence", false).forGetter(ResearchRecipe::consumeEvidence),
            Codec.BOOL.optionalFieldOf("consume_reference", false).forGetter(ResearchRecipe::consumeReference)
        ).apply(i, ResearchRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ResearchRecipe> STREAM = StreamCodec.of(
            (buf, recipe) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.evidence);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.catalyst);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.reference);
                ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                buf.writeVarInt(recipe.duration); buf.writeBoolean(recipe.consumeEvidence); buf.writeBoolean(recipe.consumeReference);
            },
            buf -> new ResearchRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                Ingredient.CONTENTS_STREAM_CODEC.decode(buf), ItemStack.STREAM_CODEC.decode(buf), buf.readVarInt(), buf.readBoolean(), buf.readBoolean()));
        @Override public MapCodec<ResearchRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, ResearchRecipe> streamCodec() { return STREAM; }
    }
}
