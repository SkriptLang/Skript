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
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util.visual;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

public class VisualEffect implements SyntaxElement, YggdrasilSerializable {

	private static final boolean HAS_REDSTONE_DATA = Skript.classExists("org.bukkit.Particle$DustOptions");

	private VisualEffectType type;

	@Nullable
	private Object data;
	private float speed = 0f;
	private float dX, dY, dZ = 0f;

	public VisualEffect() {}
	
	@SuppressWarnings({"null", "ConstantConditions"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = VisualEffects.get(matchedPattern);

		if (exprs.length > 4 && exprs[0] != null) {
			data = exprs[0].getSingle(null);
		}

		if ((parseResult.mark & 1) != 0) {
			dX = ((Number) exprs[exprs.length - 4].getSingle(null)).floatValue();
			dY = ((Number) exprs[exprs.length - 3].getSingle(null)).floatValue();
			dZ = ((Number) exprs[exprs.length - 2].getSingle(null)).floatValue();
		}

		if ((parseResult.mark & 2) != 0) {
			speed = ((Number) exprs[exprs.length - 1].getSingle(null)).floatValue();
		}

		return true;
	}

	public void play(@Nullable Player[] ps, Location l, @Nullable Entity e, int count, int radius) {
		assert e == null || l.equals(e.getLocation());

		if (type.isEffect()) {
			Effect effect = type.getEffect();
			Object data = type.getData(this.data, l);

			if (ps == null) {
				l.getWorld().playEffect(l, effect, data, radius);
			} else {
				for (Player p : ps)
					p.playEffect(l, effect, data);
			}

		} else if (type.isEntityEffect()) {
			if (e != null)
				e.playEffect(type.getEntityEffect());

		} else if (type.isParticle()) {
			Particle particle = type.getParticle();
			Object data = type.getData(this.data, l);

			// Check that data has correct type (otherwise bad things will happen)
			if (data != null && !particle.getDataType().isAssignableFrom(data.getClass())
				&& !(data instanceof ParticleOption)) {
				data = null;
				if (Skript.debug())
					Skript.warning("Incompatible particle data, resetting it!");
			}

			// Some particles use offset as RGB color codes
			if (type.isColorable() && (!HAS_REDSTONE_DATA || particle != Particle.REDSTONE) && data instanceof ParticleOption) {
				ParticleOption option = ((ParticleOption) data);
				dX = option.getRed();
				dY = option.getGreen();
				dZ = option.getBlue();
				speed = 1;
				data = null;
			}

			int loopCount = count == 0 ? 1 : count;
			if (ps == null) {
				// Colored particles must be played one at time; otherwise, colors are broken
				if (type.isColorable()) {
					for (int i = 0; i < loopCount; i++) {
						l.getWorld().spawnParticle(particle, l, 0, dX, dY, dZ, speed, data);
					}
				} else {
					l.getWorld().spawnParticle(particle, l, count, dX, dY, dZ, speed, data);
				}
			} else {
				for (Player p : ps) {
					if (type.isColorable()) {
						for (int i = 0; i < loopCount; i++) {
							p.spawnParticle(particle, l, 0, dX, dY, dZ, speed, data);
						}
					} else {
						p.spawnParticle(particle, l, count, dX, dY, dZ, speed, data);
					}
				}
			}
		} else {
			throw new IllegalStateException();
		}
	}

	public VisualEffectType getType() {
		return type;
	}

	@Override
	public String toString() {
		return toString(0);
	}
	
	public String toString(int flags) {
		return type.getName().toString(flags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, data);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VisualEffect that = (VisualEffect) o;
		return type == that.type && Objects.equals(data, that.data);
	}
	
}
