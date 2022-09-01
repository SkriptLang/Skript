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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Supplier;

@Name("Sets")
@Description("Collection sets of all the values of a type, useful for looping.")
@Examples({
	"loop all attribute types:",
	"\tset loop-value attribute of player to 10",
	"\tmessage \"Set attribute %loop-value% to 10!\""
})
@Since("INSERT VERSION")
public class ExprSets extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprSets.class, Object.class, ExpressionType.COMBINED,
			"[(all [[of] the]|the|every)] %*classinfo%");
	}

	private ClassInfo<?> classInfo;
	private Supplier<?> supplier = null;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
			classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
			supplier = classInfo.getSupplier();
			if (supplier == null) {
				Skript.error("You cannot get all " + classInfo.getName().getPlural());
				return false;
			}
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		return (Object[]) supplier.get();
	}

	@Override
	public Class<?> getReturnType() {
		return classInfo.getC();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all of the " + classInfo.getName().getPlural();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

}
