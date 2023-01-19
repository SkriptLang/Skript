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

import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

// TODO add a comparator for item types once aliases rework is done.
@Name("Spawn Reason")
@Description("The <a href='classes.html#spawnreason'>spawn reason</a> in a <a href='events.html#spawn'>spawn</a> event.")
@Examples({"on spawn:",
	"\tspawn reason is reinforcements or breeding"})
@Since("2.3")
public class ExprSpawnReason extends EventValueExpression<SpawnReason> {

	static {
		Skript.registerExpression(ExprSpawnReason.class, SpawnReason.class, ExpressionType.SIMPLE, "[the] spawn[ing] reason");
	}
	public ExprSpawnReason() {
		super(SpawnReason.class);
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "the spawning reason";
	}
}
