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
package ch.njol.skript.conditions;

import java.util.Objects;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

public class CondExpression extends Condition {
	
	private final Expression<? extends Boolean> expression;
	
	public CondExpression(Expression<? extends Boolean> expression) {
		Objects.requireNonNull(expression);
		this.expression = expression;
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		throw new IllegalStateException();
	}
	
	@Override
	public boolean check(Event e) {
		return expression.check(e, o -> o, false);
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return expression.toString(e, debug);
	}
	
	public Expression<? extends Boolean> getExpression() {
		return expression;
	}
	
}
