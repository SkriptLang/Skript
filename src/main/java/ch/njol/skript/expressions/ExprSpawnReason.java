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

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("Spawn Reason")
@Description("The <a href='classes.html#spawnreason'>spawn reason</a> in a <a href='events.html#spawn'>spawn</a> event.")
@Examples({
	"on spawn:",
		"\tspawn reason is reinforcements or breeding",
		"\tcancel event"
})
@Since("2.3")
public class ExprSpawnReason extends EventValueExpression<SpawnReason> {

	static {
		register(ExprSpawnReason.class, SpawnReason.class, "spawn[ing] reason");
	}

	public ExprSpawnReason() {
		super(SpawnReason.class);
	}

}
