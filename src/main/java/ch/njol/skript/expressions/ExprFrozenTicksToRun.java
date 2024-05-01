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
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Frozen Ticks To Run")
@Description("Gets the amount of ticks that are in queue to run while the server's tick state is frozen.")
@Examples({"broadcast \"%frozen ticks to run%\""})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprFrozenTicksToRun extends SimpleExpression<Number> {


	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			Skript.registerExpression(ExprFrozenTicksToRun.class, Number.class, ExpressionType.SIMPLE, "[the] [amount of] frozen ticks [left] to run");
		}
	}


	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		return new Number[]{ServerUtils.getServerTickManager().getFrozenTicksToRun()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "frozen ticks to run";
	}
}
