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
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("World from Name")
@Description("Returns the world from a string.")
@Examples({"world named {game::world-name}",
			"the world \"world\""})
@Since("2.6.1")
public class ExprWorldFromName extends SimpleExpression<World> {

	static {
		Skript.registerExpression(ExprWorldFromName.class, World.class, ExpressionType.SIMPLE, "[the] world [(named|with name)] %string%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<String> worldName;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worldName = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected World[] get(Event e) {
		String worldName = this.worldName.getSingle(e);
		if (worldName == null)
			return null;
		World world = Bukkit.getWorld(worldName);
		if (world == null)
			return null;

		return new World[] {world};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<World> getReturnType() {
		return World.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the world with name " + worldName.toString(e, debug);
	}

}
