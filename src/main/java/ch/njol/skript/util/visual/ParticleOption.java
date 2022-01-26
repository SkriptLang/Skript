/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
