package org.violetmoon.quark.content.building.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;

public class CelebratoryLampBlock extends ZetaBlock {

    public CelebratoryLampBlock(String regname, @Nullable ZetaModule module, Properties properties) {
        super(regname, module, properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if (pFlag.isAdvanced()) {
            pTooltip.add(1, Component.translatable("quark.misc.celebration").withStyle(ChatFormatting.GRAY));
        }
    }
}
