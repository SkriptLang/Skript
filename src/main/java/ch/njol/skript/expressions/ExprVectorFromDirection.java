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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Vectors - Create from Direction")
@Description({
	"Creates vectors from given directions.",
	"Relative directions are relative to the origin, (0, 0, 0). Therefore, the vector from the direction 'forwards' is (0, 0, 1)."
})
@Examples({
	"set {_v} to vector from direction upwards",
	"set {_v} to vector in direction of player",
	"set {_v} to vector in horizontal direction of player",
	"set {_v} to vector from facing of player",
	"set {_v::*} to vectors from north, south, east, and west"
})
@Since("INSERT VERSION")
public class ExprVectorFromDirection extends SimpleExpression<Vector> {

	static {
		Skript.registerExpression(ExprVectorFromDirection.class, Vector.class, ExpressionType.SIMPLE, "vector[s] [from|in] [direction] %directions%");
	}

	private Expression<Direction> direction;
	private static final Location DEFAULT_LOCATION = new Location(null, 0, 0, 0);

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		direction = (Expression<Direction>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Vector[] get(Event event) {
		List<Vector> vectors = new ArrayList<>();
		for (Direction dir : direction.getArray(event)) {
			vectors.add(dir.getDirection(DEFAULT_LOCATION)); // all relative directions are relative to the default location
		}
		return vectors.toArray(new Vector[0]);
	}

	@Override
	public boolean isSingle() {
		return direction.isSingle();
	}

	@Override
	public Class<? extends Vector> getReturnType() {
		return Vector.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "vector from direction " + direction.toString(event, debug);
	}
}
