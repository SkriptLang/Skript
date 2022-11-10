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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Value Within")
@Description("Gets the value within objects. Usually used with variables to get the value they store rather than the variable itself, " +
	"or with lists to get the values of a type.")
@Examples({
	"set {_entity} to a random entity out of all entities",
	"delete entity within {_entity} # This deletes the entity itself and not the value stored in the variable",
	"",
	"set {_list::*} to \"something\", 10, \"test\" and a zombie",
	"broadcast the strings within {_list::*} # \"something\", \"test\""
})
@Since("INSERT VERSION")
public class ExprValueWithin extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprValueWithin.class, Object.class, ExpressionType.SIMPLE, "[the] (%-*classinfo%|value[s]) (within|in) %objects%");
	}

	@Nullable
	private ClassInfo<?> classInfo;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		classInfo = exprs[0] == null ? null : ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		Expression<?> expr = exprs[0] == null ? exprs[1] : exprs[1].getConvertedExpression(classInfo.getC());
		if (expr == null)
			return false;
		setExpr(expr);
		return true;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return classInfo == null ? "value" : classInfo.toString(e, debug) + " within " + getExpr();
	}

}
