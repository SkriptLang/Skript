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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.sections.EffSecCreateWorldBorder;
import ch.njol.util.Kleenean;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Last World Border")
@Description("The last created world border from a 'world border creation' section.")
@Examples({
	"create a world border:",
	"\tset center of border to player's location",
	"set player's world border to the last created world border"
})
@Since("INSERT VERSION")
@RequiredPlugins("1.18+")
public class ExprLastWorldBorder extends SimpleExpression<WorldBorder> {

	static {
		if (Skript.methodExists(Player.class, "getWorldBorder"))
			Skript.registerExpression(ExprLastWorldBorder.class, WorldBorder.class, ExpressionType.SIMPLE, "[the] last[ly] created [world[ ]]border");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected WorldBorder[] get(Event event) {
		return new WorldBorder[] {EffSecCreateWorldBorder.lastCreatedWorldBorder};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends WorldBorder> getReturnType() {
		return WorldBorder.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the lastly created world border";
	}

}
