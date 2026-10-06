package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.violetmoon.zetaimplfabric.ext.client.IZetaKeyMappingExtensions;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public abstract class KeyMappingExtMixin implements IZetaKeyMappingExtensions {

	@Shadow private InputConstants.Key key;

	@Override
	public InputConstants.Key getKey() {
		return key;
	}
}
