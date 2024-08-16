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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Portal Cooldown")
@Description({
	"The amount of time before an entity can use a portal. By default, it is 15 seconds after exiting a nether portal or end gateway.",
	"Players in survival/adventure get a cooldown of 0.5 seconds, while those in creative get no cooldown.",
	"Resetting will set the cooldown back to the default 15 seconds for non-player entities and 0.5 seconds for players."
})
@Examples({
	"on portal:",
		"\twait 1 tick",
		"\tset portal cooldown of event-entity to 5 seconds"
})
@Since("2.8.0")
public class ExprPortalCooldown extends SimplePropertyExpression<Entity, Timespan> {

	static {
		register(ExprPortalCooldown.class, Timespan.class, "portal cooldown", "entities");
	}

	// Default cooldown for nether portals is 15 seconds:
	// https://minecraft.fandom.com/wiki/Nether_portal#Behavior
	private static final int DEFAULT_COOLDOWN = 15 * 20;
	// Players only get a 0.5 second cooldown in survival/adventure:
	private static final int DEFAULT_COOLDOWN_PLAYER = 10;

	@Override
	@Nullable
	public Timespan convert(Entity entity) {
		return Timespan.fromTicks(entity.getPortalCooldown());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case RESET:
			case DELETE:
			case REMOVE:
				return CollectionUtils.array(Timespan.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] entities = getExpr().getArray(event);
		int change = delta == null ? 0 : (int) ((Timespan) delta[0]).getTicks_i();
		switch (mode) {
			case REMOVE:
				change = -change; // allow fall-through to avoid duplicate code
			case ADD:
				for (Entity entity : entities) {
					entity.setPortalCooldown(Math.max(entity.getPortalCooldown() + change, 0));
				}
				break;
			case RESET:
				for (Entity entity : entities) {
					// Players in survival/adventure get a 0.5 second cooldown, while those in creative get no cooldown
					if (entity instanceof Player) {
						if (((Player) entity).getGameMode() == GameMode.CREATIVE) {
							entity.setPortalCooldown(0);
						} else {
							entity.setPortalCooldown(DEFAULT_COOLDOWN_PLAYER);
						}
					// Non-player entities get a 15 second cooldown
					} else {
						entity.setPortalCooldown(DEFAULT_COOLDOWN);
					}
				}
				break;
			case DELETE:
			case SET:
				for (Entity entity : entities) {
					entity.setPortalCooldown(Math.max(change, 0));
				}
				break;
			default:
				assert false;
		}
	}

	@Override
	public Class<? extends Timespan> getReturnType() {
		return Timespan.class;
	}

	@Override
	protected String getPropertyName() {
		return "portal cooldown";
	}

}
