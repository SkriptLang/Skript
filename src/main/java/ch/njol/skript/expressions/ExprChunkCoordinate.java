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

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Chunk;
import org.eclipse.jdt.annotation.Nullable;

@Name("Coordinate of Chunk")
@Description("The X/Y coordinate of the <a href='./classes.html#chunk'>chunk</a>")
@Examples({
	"send chunk at the player's x coordinate",
	"send chunk at the player's z coordinate"
})
@Since("INSERT VERSION")


public class ExprChunkCoordinate extends SimplePropertyExpression<Chunk, Integer> {

	static {
		register(ExprChunkCoordinate.class, Integer.class, "(0¦x|1¦z)(-| )(coord[inate])", "chunks");
	}

	private boolean isX;
	private boolean isZ;


	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isX = parseResult.mark == 0;
		isZ = parseResult.mark == 1;
		setExpr((Expression<? extends Chunk>) exprs[0]);
		return true;
	}

	@Override
	protected String getPropertyName() {
		return isX ? "x" : "z" + "coordinate of " + getExpr();
	}

	@Override
	public @Nullable Integer convert(Chunk chunk) {
		if (isX)
			return chunk.getX();
		else if (isZ)
			return chunk.getZ();
		return null;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
}
