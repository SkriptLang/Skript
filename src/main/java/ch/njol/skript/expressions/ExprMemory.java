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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Free / Max / Total Memory")
@Description("The free, max or total memory of the server in MB.")
@Examples({
	"while player is online:",
		"\tsend action bar \"Memory left: %free memory%MB\" to player",
		"\twait 5 ticks"
})
@Since("INSERT VERSION")
public class ExprMemory extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprMemory.class, Number.class, ExpressionType.SIMPLE, "[the] (:free|:max[imum]|total) (memory|ram)");
	}

	private static final Runtime RUNTIME = Runtime.getRuntime();
	private Type type;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.hasTag("free")) {
			type = Type.FREE;
		} else if (parseResult.hasTag("max")) {
			type = Type.MAXIMUM;
		} else {
			type = Type.TOTAL;
		}
		return true;
	}

	@Override
	protected @Nullable Number[] get(Event event) {
		Number memory = 0;
		switch (type) {
			case FREE:
				memory = RUNTIME.freeMemory();
				break;
			case MAXIMUM:
				memory = RUNTIME.maxMemory();
				break;
			case TOTAL:
				memory = RUNTIME.totalMemory();
		}
		return CollectionUtils.array(memory.longValue() * 1.024e-6);
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
		return (type == Type.FREE ? "free" : type == Type.MAXIMUM ? "maximum" : "total") + " memory";
	}

	private enum Type {
		FREE, MAXIMUM, TOTAL
	}

}
