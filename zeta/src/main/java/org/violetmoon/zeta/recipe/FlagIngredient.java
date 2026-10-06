package org.violetmoon.zeta.recipe;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.config.ConfigFlagManager;

import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author WireSegal
 * Created at 3:44 PM on 10/20/19.
 *
 * Fabric: vanilla Ingredient is final, so this is a Fabric CustomIngredient. Use {@link #toVanilla()} to get an Ingredient.
 * In JSON it is referenced with "fabric:type" instead of Forge's "type".
 */
public class FlagIngredient implements CustomIngredient {

	private final Ingredient parent;

	private final ConfigFlagManager cfm;
	private final String flag;
	private final Serializer serializer;

	public FlagIngredient(Ingredient parent, String flag, ConfigFlagManager cfm, Serializer serializer) {
		this.parent = parent;
		this.cfm = cfm;
		this.flag = flag;
		this.serializer = serializer;
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		if (!cfm.getFlag(flag))
			return List.of();
		return Arrays.asList(parent.getItems());
	}

	@Override
	public boolean test(@Nullable ItemStack target) {
		if (target == null || !cfm.getFlag(flag))
			return false;

		return parent.test(target);
	}

	@Override
	public boolean requiresTesting() {
		// the flag can change at runtime, so matching must always go through test()
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return serializer;
	}

	public record Serializer(ConfigFlagManager cfm) implements CustomIngredientSerializer<FlagIngredient> {

		@Deprecated(forRemoval = true)
		public static Serializer INSTANCE;

		@Override
		public ResourceLocation getIdentifier() {
			return new ResourceLocation(cfm.zeta.modid, "flag");
		}

		@NotNull
		@Override
		public FlagIngredient read(@NotNull FriendlyByteBuf buffer) {
			return new FlagIngredient(Ingredient.fromNetwork(buffer), buffer.readUtf(), cfm, this);
		}

		@NotNull
		@Override
		public FlagIngredient read(@NotNull JsonObject json) {
			Ingredient value = Ingredient.fromJson(json.get("value"));
			String flag = json.getAsJsonPrimitive("flag").getAsString();
			return new FlagIngredient(value, flag, cfm, this);
		}

		@Override
		public void write(@NotNull JsonObject json, @NotNull FlagIngredient ingredient) {
			json.add("value", ingredient.parent.toJson());
			json.addProperty("flag", ingredient.flag);
		}

		@Override
		public void write(@NotNull FriendlyByteBuf buffer, @NotNull FlagIngredient ingredient) {
			ingredient.parent.toNetwork(buffer);
			buffer.writeUtf(ingredient.flag);
		}

		public Zeta getZeta() {
			return cfm.zeta;
		}
	}
}
