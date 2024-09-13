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
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Charge Entity")
@Description("Charges or uncharges a creeper or wither skull. A creeper is charged when it has been struck by lightning.")
@Examples({
	"on spawn of creeper:",
		"\tcharge the event-entity"
})
@Since("2.5")
public class EffCharge extends Effect {

	static {
		Skript.registerEffect(EffCharge.class,
				"make %entities% [un:(un|not |non[-| ])](charged|powered)",
				"[:un](charge|power) %entities%");
	}

	@SuppressWarnings("null")
	private Expression<Entity> entities;

	private boolean charge;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		charge = !parseResult.hasTag("un");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof Creeper) {
				((Creeper) entity).setPowered(charge);
			} else if (entity instanceof WitherSkull) {
				((WitherSkull) entity).setCharged(charge);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (charge ? " charged" : " not charged");
	}

}
