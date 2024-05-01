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
import ch.njol.skript.classes.Changer.ChangeMode;
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
import org.jetbrains.annotations.Nullable;

@Name("Server Tick Rate")
@Description({
	"Gets or sets the current tick rate of the server. The tick rate is the number of game ticks that occur in a second. Higher values mean the game runs faster.",
	"The server's default tick rate is 20."})
@Examples({
	"send \"%server's tick rate%\" to player",
	"set server's tick rate to 20 # This is the default tick rate.",
	"add 5 to server's tick rate",
	"remove 2 from server's tick rate"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprTick extends SimpleExpression<Number> {


	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			Skript.registerExpression(ExprTick.class, Number.class, ExpressionType.SIMPLE, "[the] server['s] tick rate");
		}
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Nullable
	@Override
	protected Number[] get(Event event) {
		return new Number[]{ServerUtils.getServerTickManager().getTickRate()};
	}

	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET)
			return new Class[]{Number.class};
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		float tickRate = ServerUtils.getServerTickManager().getTickRate();
		float change = delta != null && delta.length != 0 ? ((Number) delta[0]).floatValue() : 0;
		switch (mode) {
			case SET:
				if (delta != null) {
					ServerUtils.getServerTickManager().setTickRate(change);
				}
				break;
			case ADD:
				if (delta != null) {
					ServerUtils.getServerTickManager().setTickRate(tickRate + change);
				}
				break;
			case REMOVE:
				if (delta != null) {
					ServerUtils.getServerTickManager().setTickRate(tickRate - change);
				}
				break;
			case RESET:
				ServerUtils.getServerTickManager().setTickRate(20);
				break;
		}
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
		return "server tick rate";
	}

}
