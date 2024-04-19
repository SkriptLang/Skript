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
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Value Within")
@Description(
	"Gets the value within objects. Usually used with variables to get the value they store rather than the variable itself, " +
	"or with lists to get the values of a type."
)
@Examples({
	"set {_entity} to a random entity out of all entities",
	"delete entity within {_entity} # This deletes the entity itself and not the value stored in the variable",
	"",
	"set {_list::*} to \"something\", 10, \"test\" and a zombie",
	"broadcast the strings within {_list::*} # \"something\", \"test\""
})
@Since("2.7")
public class ExprValueWithin extends WrapperExpression<Object> {

	static {
		Skript.registerExpression(ExprValueWithin.class, Object.class, ExpressionType.COMBINED, "[the] (%-*classinfo%|value[:s]) (within|in) %~objects%");
	}

	@Nullable
	private ClassInfo<?> classInfo;

	@Nullable
	@SuppressWarnings("rawtypes")
	private Changer changer;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean plural;
		if (exprs[0] != null) {
			Literal<ClassInfoReference> classInfoReference = (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) exprs[0]);
			plural = classInfoReference.getSingle().isPlural().isTrue();
		} else {
			plural = parseResult.hasTag("s");
		}

		if (plural == exprs[1].isSingle()) {
			if (plural) {
				Skript.error("You cannot get multiple elements of a single value");
			} else {
				Skript.error(exprs[1].toString(null, false) + " may contain more than one " + (classInfo == null ? "value" :  classInfo.getName()));
			}
			return false;
		}

		classInfo = exprs[0] == null ? null : ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		Expression<?> expr = classInfo == null ? exprs[1] : exprs[1].getConvertedExpression(classInfo.getC());
		if (expr == null)
			return false;
		setExpr(expr);
		return true;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		changer = Classes.getSuperClassInfo(getReturnType()).getChanger();
		if (changer == null)
			return null;
		return changer.acceptChange(mode);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (changer == null)
			throw new UnsupportedOperationException();
		changer.change(getArray(event), delta, mode);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (classInfo == null ? "value" : classInfo.toString(event, debug)) + " within " + getExpr();
	}

}
