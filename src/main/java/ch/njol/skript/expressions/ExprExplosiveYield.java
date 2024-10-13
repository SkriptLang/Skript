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
package ch.njol.skript.expressions;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Explosive Yield")
@Description({"The yield of an explosive (creeper, primed tnt, fireball, etc.). This is how big of an explosion is caused by the entity.",
				"Read <a href='https://minecraft.wiki/w/Explosion'>this wiki page</a> for more information"})
@Examples({"on spawn of a creeper:",
			"\tset the explosive yield of the event-entity to 10"})
@RequiredPlugins("Minecraft 1.12 or newer for creepers")
@Since("2.5")
public class ExprExplosiveYield extends SimplePropertyExpression<Entity, Number> {

	static {
		register(ExprExplosiveYield.class, Number.class, "explosive (yield|radius|size)", "entities");
	}

	private final static boolean CREEPER_USABLE = Skript.methodExists(Creeper.class, "getExplosionRadius");

	@Override
	public Number convert(Entity e) {
		if (e instanceof Explosive)
			return ((Explosive) e).getYield();
		if (CREEPER_USABLE && e instanceof Creeper)
			return ((Creeper) e).getExplosionRadius();
		return 0;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
				return CollectionUtils.array(Number.class);
			default:
				return null;
		}
	}

	@Override
	public void change(final Event event, final @Nullable Object[] delta, final ChangeMode mode) {
		Number change = delta != null ? (Number) delta[0] : 0;
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Explosive) {
				Explosive e = (Explosive) entity;
				float f = change.floatValue();
				if (f < 0) // Negative values will throw an error.
					return;
				switch (mode) {
					case SET:
						e.setYield(f);
						break;
					case ADD:
						float add = e.getYield() + f;
						if (add < 0)
							return;
						e.setYield(add);
						break;
					case REMOVE:
						float subtract = e.getYield() - f;
						if (subtract < 0)
							return;
						e.setYield(subtract);
						break;	
					case DELETE:
						e.setYield(0);
						break;
					default:
						assert false;
				}
			} else if (CREEPER_USABLE && entity instanceof Creeper) {
				Creeper c = (Creeper) entity;
				int i = change.intValue();
				if (i < 0) // Negative values will throw an error.
					return;
				switch (mode) {
					case SET:
						c.setExplosionRadius(i);
						break;
					case ADD:
						int add = c.getExplosionRadius() + i;
						if (add < 0)
							return;
						c.setExplosionRadius(add);
						break;
					case REMOVE:
						int subtract = c.getExplosionRadius() - i;
						if (subtract < 0)
							return;
						c.setExplosionRadius(subtract);
						break;	
					case DELETE:
						c.setExplosionRadius(0);
						break;
					case REMOVE_ALL:
					case RESET:
						assert false;
				}
			}
		}
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "explosive yield";
	}

}
