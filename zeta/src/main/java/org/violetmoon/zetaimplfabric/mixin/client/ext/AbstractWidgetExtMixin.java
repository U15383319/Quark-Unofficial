package org.violetmoon.zetaimplfabric.mixin.client.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.violetmoon.zetaimplfabric.ext.client.IZetaWidgetExtensions;

import net.minecraft.client.gui.components.AbstractWidget;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetExtMixin implements IZetaWidgetExtensions { }
