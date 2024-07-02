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
package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Armor Stand - Properties")
@Description("Allows users to modify properties of an armor stand (i.e. whether it's small or a marker).")
@Examples({
	"make {_armorstand} small",
	"make {_armorstands::*} a marker"
})
@Since("INSERT VERSION")
public class EffArmorStandProperties extends Effect {

	static {
		Skript.registerEffect(EffArmorStandProperties.class,
			"make %livingentities% [:not] (:small|a marker)"
		);
	}

	private boolean not;
	private boolean small;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		not = parseResult.hasTag("not");
		small = parseResult.hasTag("small");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof ArmorStand) {
				if (small) {
					((ArmorStand) entity).setSmall(!not);
				} else {
					((ArmorStand) entity).setMarker(!not);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (not ? " not " : " ") + (small ? "small" : "a marker");
	}
}
