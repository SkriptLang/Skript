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

@Name("Has Relational Variable")
@Description({"Checks whether the given relation variables are present on the given holders.",
			"See <a href='classes.html#persistentdataholder'>persistent data holder</a> for a list of all holders."
})
@Examples({"player holds relational variable {isAdmin}",
			"player holds relational variable {oldNames::*}"})
@RequiredPlugins("1.14 or newer")
@Since("INSERT VERSION")
public class CondHasRelationalVariable extends Condition {

	static {
		if (Skript.classExists("org.bukkit.persistence.PersistentDataHolder")) {
			Skript.registerCondition(CondHasRelationalVariable.class,
					"%persistentdataholders/itemtypes/blocks% (has|have|holds) [(relational|relation( |-)based) variable[s]] %objects%",
					"%persistentdataholders/itemtypes/blocks% (doesn't|does not|do not|don't) (have|hold) [(relational|relation( |-)based) variable[s]] %objects%"
			);
		}
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Object> holders;
	@SuppressWarnings("NotNullFieldNotInitialized")
	private ExpressionList<?> variables;

	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ExpressionList<?> exprList = exprs[1] instanceof ExpressionList ? (ExpressionList<?>) exprs[1] : new ExpressionList<>(new Expression<?>[]{exprs[1]}, Object.class, false);
		for (Expression<?> expr : exprList.getExpressions()) {
			if (!(expr instanceof Variable<?>)) { // Input not a variable
				return false;
			} else if (((Variable<?>) expr).isLocal()) { // Input is a variable, but it's local
				Skript.error("Setting a relational variable using a local variable is not supported."
						+ " If you are trying to set a value temporarily, consider using metadata", ErrorQuality.SEMANTIC_ERROR
				);
				return false;
			}
		}
		variables = exprList;
		holders = (Expression<Object>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		for (Expression<?> expr : variables.getExpressions()) {
			if (!(holders.check(e, holder -> PersistentDataUtils.has(((Variable<?>) expr).getName().toString(e), holder), isNegated())))
				return false;
		}
		return true;

	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, holders,
			"relational variable(s) " + variables.toString(e, debug));
	}

}
