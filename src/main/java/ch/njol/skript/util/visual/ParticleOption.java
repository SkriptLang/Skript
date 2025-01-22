package ch.njol.skript.util.visual;

import ch.njol.skript.util.Color;

public class ParticleOption {

	org.bukkit.Color color;
	float size;

	public ParticleOption(Color color, float size) {
		this.color = color.asBukkitColor();
		this.size = size;
	}

	public org.bukkit.Color getBukkitColor() {
		return color;
	}

	public float getRed() {
		return (float) color.getRed() / 255.0f;
	}

	public float getGreen() {
		return (float) color.getGreen() / 255.0f;
	}

	public float getBlue() {
		return (float) color.getBlue() / 255.0f;
	}

	@Override
	public String toString() {
		return "ParticleOption{color=" + color + ", size=" + size + "}";
	}

}
