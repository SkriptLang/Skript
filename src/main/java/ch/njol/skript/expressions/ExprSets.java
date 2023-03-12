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
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import com.google.common.collect.Lists;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Name("Sets")
@Description("Returns a list of all the values of a type; useful for looping.")
@Examples({
	"loop all attribute types:",
	"\tset loop-value attribute of player to 10",
	"\tmessage \"Set attribute %loop-value% to 10!\""
})
@Since("<i>unknown</i> (before 1.4.2), 2.7 (colors)")
public class ExprSets extends SimpleExpression<Object> {

	static {
		Skript.registerExpression(ExprSets.class, Object.class, ExpressionType.COMBINED,
			"[all [[of] the]|the|every] %*classinfo%"
		);
	}

	private ClassInfo<?> classInfo;
	@Nullable
	private Supplier<? extends Iterator<?>> supplier;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		// This check makes sure that "color" is not a valid pattern, and the type the user inputted has to be plural, unless it's "every %classinfo%"
		boolean plural = Utils.getEnglishPlural(parser.expr).getSecond();
		if (!plural && !parser.expr.startsWith("every"))
			return false;

		classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		supplier = classInfo.getSupplier();
		if (supplier == null) {
			Skript.error("You cannot get all values of type '" + classInfo.getName().getSingular() + "'");
			return false;
		}
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		assert supplier != null;
		Iterator<?> iterator = supplier.get();
		List<?> elements = Lists.newArrayList(iterator);
		return elements.toArray(new Object[0]);
	}

	@Override
	@Nullable
	public Iterator<?> iterator(Event event) {
		assert supplier != null;
		return supplier.get();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<?> getReturnType() {
		return classInfo.getC();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all of the " + classInfo.getName().getPlural();
	}

}
