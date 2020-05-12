/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */

package ch.njol.skript.conditions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.PersistentDataUtils;
import ch.njol.util.Kleenean;

@Name("Has Persistent Data")
@Description({"Checks whether a persistent data holder has the specified value.",
			"This condition will return true if the value also exists under metadata.",
			"See <a href='classes.html#persistentdataholder'>persistent data holder</a> for a list of all holders."})
@Examples("if player has persistent data \"epic\":")
@RequiredPlugins("1.14 or newer")
@Since("INSERT VERSION")
public class CondHasPersistentData extends Condition {

	static {
		if (Skript.isRunningMinecraft(1, 14)) {
			Skript.registerCondition(CondHasPersistentData.class,
					"%persistentdataholders/itemtypes/blocks% (has|have) persistent data [(value|tag)[s]] %objects%",
					"%persistentdataholders/itemtypes/blocks% (doesn't|does not|do not|don't) have persistent data [(value|tag)[s]] %objects%"
			);
		}
	}

	@SuppressWarnings("null")
	private Expression<Object> holders;
	@SuppressWarnings("null")
	private Expression<Object> varExpression;

	@SuppressWarnings("null")
	private Variable<?>[] variables;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		List<Variable<?>> vars = new ArrayList<>();
		ExpressionList<?> exprList = exprs[1] instanceof ExpressionList ? (ExpressionList<?>) exprs[1] : new ExpressionList<>(new Expression<?>[]{exprs[1]}, Object.class, false);
		for (Expression<?> expr : exprList.getExpressions()) {
			if (expr instanceof Variable<?>) {
				Variable<?> v = (Variable<?>) expr;
				if (v.isLocal()) {
					Skript.error("Using local variables in Persistent Data is not supported."
								+ " If you are trying to set a value temporarily, consider using Metadata", ErrorQuality.SEMANTIC_ERROR
					);
					return false;
				}
				vars.add(v);
			}
		}
		if (!vars.isEmpty()) {
			setNegated(matchedPattern == 1);
			variables = vars.toArray(new Variable<?>[0]);
			varExpression = (Expression<Object>) exprs[1];
			holders = (Expression<Object>) exprs[0];
			return true;
		}
		Skript.error("Persistent Data values are formatted as variables (e.g. \"persistent data value {isAdmin}\")" , ErrorQuality.SEMANTIC_ERROR);
		return false;
	}

	@Override
	public boolean check(Event e) {
		for (Variable<?> v : variables) {
			if (!(holders.check(e, holder -> PersistentDataUtils.has(holder, v.getName().toString(e)), isNegated())))
				return false;
		}
		return true;

	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, holders,
				"persistent data " + (varExpression.isSingle() ? "value " : "values ") + varExpression.toString(e, debug));
	}

}
