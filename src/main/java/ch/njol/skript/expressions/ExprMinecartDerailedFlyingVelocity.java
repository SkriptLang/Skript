/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Minecart Derailed / Flying Velocity")
@Description("The velocity of a minecart as soon as it has been derailed or as soon as it starts flying.")
@Examples({"on right click on minecart:",
	"\tset derailed velocity of event-entity to vector 2, 10, 2"})
@Since("2.5.1")
public class ExprMinecartDerailedFlyingVelocity extends SimplePropertyExpression<Entity, Vector> {
	
	static {
		register(ExprMinecartDerailedFlyingVelocity.class, Vector.class,
			"[minecart] (1¦derailed|2¦flying) velocity", "entities");
	}
	
	private boolean flying;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		flying = parseResult.mark == 2;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Nullable
	@Override
	public Vector convert(Entity entity) {
		if (entity instanceof Minecart) {
			Minecart mc = (Minecart) entity;
			return flying ? mc.getFlyingVelocityMod() : mc.getDerailedVelocityMod();
		}
		return null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
				return CollectionUtils.array(Vector.class);
			default:
				return null;
		}
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta != null) {
			if (flying) {
				switch (mode) {
					case SET:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart)
								((Minecart) entity).setFlyingVelocityMod((Vector) delta[0]);
						}
						break;
					case ADD:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart) {
								Minecart minecart = (Minecart) entity;
								minecart.setFlyingVelocityMod(((Vector) delta[0]).add(minecart.getFlyingVelocityMod()));
							}
						}
						break;
					case REMOVE:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart) {
								Minecart minecart = (Minecart) entity;
								minecart.setFlyingVelocityMod(((Vector) delta[0]).subtract(minecart.getFlyingVelocityMod()));
							}
						}
						break;
					default:
						assert false;
				}
			} else {
				switch (mode) {
					case SET:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart)
								((Minecart) entity).setDerailedVelocityMod((Vector) delta[0]);
						}
						break;
					case ADD:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart) {
								Minecart minecart = (Minecart) entity;
								minecart.setDerailedVelocityMod(((Vector) delta[0]).add(minecart.getDerailedVelocityMod()));
							}
						}
						break;
					case REMOVE:
						for (Entity entity : getExpr().getArray(e)) {
							if (entity instanceof Minecart) {
								Minecart minecart = (Minecart) entity;
								minecart.setDerailedVelocityMod(((Vector) delta[0]).subtract(minecart.getDerailedVelocityMod()));
							}
						}
						break;
					default:
						assert false;
				}
			}
		}
	}
	
	@Override
	protected String getPropertyName() {
		return (flying ? "flying" : "derailed") + " velocity";
	}
	
	
	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}
	
}
