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
package ch.njol.skript.conditions;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is in World")
@Description("Checks whether an entity is in a specific world.")
@Examples({"player is in \"world\"",
		"argument isn't in world \"world_nether\"",
		"the player is in the world of the victim"})
@Since("1.4")
public class CondIsInWorld extends Condition {
	
	static {
		PropertyCondition.register(CondIsInWorld.class, "in [[the] world[s]] %worlds%", "entities");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	@SuppressWarnings("null")
	private Expression<World> worlds;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		worlds = (Expression<World>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return entities.check(e,
				entity -> worlds.check(e,
						world -> entity.getWorld() == world
				), isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, entities,
				"in the " + (worlds.isSingle() ? "world " : "worlds ") + worlds.toString(event, debug));
	}
	
}
