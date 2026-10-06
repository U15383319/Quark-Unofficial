package org.violetmoon.zetaimplfabric.mixin.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

/**
 * Forge lets cooking recipes give a stack: {@code "result": {"item": ..., "count": ...}} instead of an item id.
 */
@Mixin(SimpleCookingSerializer.class)
public class SimpleCookingSerializerExtMixin {

	private static final String FROM_JSON = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;";

	@WrapOperation(method = FROM_JSON, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;getAsString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;"))
	private String zeta$stackResult(JsonObject json, String key, Operation<String> original, @Share("count") LocalIntRef count) {
		JsonElement result = json.get(key);
		if("result".equals(key) && result != null && result.isJsonObject()) {
			JsonObject stack = result.getAsJsonObject();
			count.set(GsonHelper.getAsInt(stack, "count", 1));
			return GsonHelper.getAsString(stack, "item");
		}
		count.set(1);
		return original.call(json, key);
	}

	@ModifyExpressionValue(method = FROM_JSON, at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack"))
	private ItemStack zeta$stackCount(ItemStack stack, @Share("count") LocalIntRef count) {
		if(count.get() > 1)
			stack.setCount(count.get());
		return stack;
	}
}
