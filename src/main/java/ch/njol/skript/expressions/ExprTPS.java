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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("TPS (ticks per second)")
@Description("Returns the 3 most recent TPS readings, like the /tps command. " +
			"This expression is only supported on some server software (PaperSpigot).")
@Examples("broadcast \"%tps%\"")
@Since("2.2-dev36")
public class ExprTPS extends SimpleExpression<Number> {

	private static final boolean SUPPORTED = Skript.methodExists(Server.class, "getTPS");
	private int index;
	private String expr = "tps";

	static {
		Skript.registerExpression(ExprTPS.class, Number.class, ExpressionType.SIMPLE,
				"tps from [the] last ([1] minute|1[ ]m[inute])",
				"tps from [the] last 5[ ]m[inutes]",
				"tps from [the] last 15[ ]m[inutes]",
				"[the] tps");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!SUPPORTED) {
			Skript.error("The TPS expression is not supported on this server software");
			return false;
		}
		expr = parseResult.expr;
		index = matchedPattern;
		return true;
	}

	@Override
	protected Number[] get(Event e) {
		double[] tps = Bukkit.getServer().getTPS();
		if (index != 3) {
			return new Number[] { tps[index] };
		}
		return CollectionUtils.wrap(tps);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean isSingle() {
		return index != 3;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return expr;
	}

}
