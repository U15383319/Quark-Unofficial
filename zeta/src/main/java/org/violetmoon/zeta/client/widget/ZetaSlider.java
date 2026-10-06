package org.violetmoon.zeta.client.widget;

import java.text.DecimalFormat;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Fabric stand-in for Forge's ForgeSlider: a slider over [minValue, maxValue] with an optional step size.
 */
public class ZetaSlider extends AbstractSliderButton {

	protected Component prefix;
	protected Component suffix;
	protected double minValue;
	protected double maxValue;
	protected double stepSize;
	protected boolean drawString;
	private final DecimalFormat format;

	public ZetaSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue,
					  double currentValue, double stepSize, int precision, boolean drawString) {
		super(x, y, width, height, Component.empty(), 0D);
		this.prefix = prefix;
		this.suffix = suffix;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stepSize = Math.abs(stepSize);
		this.value = snapToNearest((currentValue - minValue) / (maxValue - minValue));
		this.drawString = drawString;

		if(stepSize == 0) {
			precision = Math.min(precision, 4);
			StringBuilder builder = new StringBuilder("0");
			if(precision > 0)
				builder.append('.');
			while(precision-- > 0)
				builder.append('0');
			this.format = new DecimalFormat(builder.toString());
		} else if(Mth.equal(this.stepSize, Math.floor(this.stepSize)))
			this.format = new DecimalFormat("0");
		else
			this.format = new DecimalFormat(Double.toString(this.stepSize).replaceAll("\\d", "0"));

		updateMessage();
	}

	public double getValue() {
		return this.value * (maxValue - minValue) + minValue;
	}

	public long getValueLong() {
		return Math.round(getValue());
	}

	public int getValueInt() {
		return (int) getValueLong();
	}

	public void setValue(double value) {
		this.value = snapToNearest((value - minValue) / (maxValue - minValue));
		updateMessage();
	}

	public String getValueString() {
		return format.format(getValue());
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		setValueFromMouse(mouseX);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		super.onDrag(mouseX, mouseY, dragX, dragY);
		setValueFromMouse(mouseX);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean flag = keyCode == 263; // left
		if(flag || keyCode == 262) { // right
			if(minValue > maxValue)
				flag = !flag;
			float f = flag ? -1F : 1F;
			if(stepSize <= 0D)
				setSliderValue(this.value + (f / (width - 8)));
			else
				setValue(getValue() + f * stepSize);
		}
		return false;
	}

	private void setValueFromMouse(double mouseX) {
		setSliderValue((mouseX - (getX() + 4)) / (width - 8));
	}

	private void setSliderValue(double value) {
		double oldValue = this.value;
		this.value = snapToNearest(value);
		if(!Mth.equal(oldValue, this.value))
			applyValue();
		updateMessage();
	}

	private double snapToNearest(double value) {
		if(stepSize <= 0D)
			return Mth.clamp(value, 0D, 1D);

		value = Mth.lerp(Mth.clamp(value, 0D, 1D), minValue, maxValue);
		value = (stepSize * Math.round(value / stepSize));
		if(minValue > maxValue)
			value = Mth.clamp(value, maxValue, minValue);
		else
			value = Mth.clamp(value, minValue, maxValue);
		return Mth.map(value, minValue, maxValue, 0D, 1D);
	}

	@Override
	protected void updateMessage() {
		if(drawString)
			setMessage(Component.literal("").append(prefix).append(getValueString()).append(suffix));
		else
			setMessage(Component.empty());
	}

	@Override
	protected void applyValue() { }
}
