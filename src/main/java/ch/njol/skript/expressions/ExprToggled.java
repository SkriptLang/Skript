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

import java.util.Arrays;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

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

@Name("Toggled Boolean")
@Description("The toggled boolean value.")
@Examples("set {_gravity} to toggled player's gravity")
@Since("INSERT VERSION")
public class ExprToggled extends SimpleExpression<Boolean> {

	static {
		Skript.registerExpression(ExprToggled.class, Boolean.class, ExpressionType.COMBINED, "(toggled|switched) %booleans%");
	}

	private Expression<Boolean> booleanExpr;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		booleanExpr = (Expression<Boolean>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected Boolean[] get(Event e) {
		boolean[] original = booleanExpr.getArray(e);
		boolean[] toggled = new boolean[original.length];
		for (int i = 0; i < original.length; i++)
			toggled[i] = !original[i];
		return toggled;
	}

	@Override
	public boolean isSingle() {
		return booleanExpr.isSingle();
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "toggled " + booleanExpr.toString(e, debug);
	}

}
