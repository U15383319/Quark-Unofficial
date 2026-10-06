package org.violetmoon.zetaimplfabric.ext.client;

import com.mojang.blaze3d.platform.InputConstants;

/**
 * Forge's KeyMapping#getKey, injected with Loom interface injection.
 */
public interface IZetaKeyMappingExtensions {
	default InputConstants.Key getKey() {
		throw new AssertionError("implemented by KeyMappingExtMixin");
	}
}
