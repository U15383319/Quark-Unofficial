package org.violetmoon.zeta.block;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

/**
 * Fabric stand-in for Forge's ForgeSoundType: a SoundType whose sounds are looked up lazily, so it can be created
 * before the sound events are registered.
 */
public class ZetaSoundType extends SoundType {

	private final Supplier<SoundEvent> breakSound, stepSound, placeSound, hitSound, fallSound;

	public ZetaSoundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
						 Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
		super(volume, pitch, null, null, null, null, null);
		this.breakSound = breakSound;
		this.stepSound = stepSound;
		this.placeSound = placeSound;
		this.hitSound = hitSound;
		this.fallSound = fallSound;
	}

	@Override
	public SoundEvent getBreakSound() {
		return breakSound.get();
	}

	@Override
	public SoundEvent getStepSound() {
		return stepSound.get();
	}

	@Override
	public SoundEvent getPlaceSound() {
		return placeSound.get();
	}

	@Override
	public SoundEvent getHitSound() {
		return hitSound.get();
	}

	@Override
	public SoundEvent getFallSound() {
		return fallSound.get();
	}
}
