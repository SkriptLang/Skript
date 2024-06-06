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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jaxen.expr.Expr;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.Set;

@Name("Filter Input")
@Description({
	"Represents the input in a filter expression.",
	"For example, if you ran 'broadcast \"something\" and \"something else\" where [input is \"something\"]",
	"the condition would be checked twice, using \"something\" and \"something else\" as the inputs."
})
@Examples("send \"congrats on being staff!\" to all players where [input has permission \"staff\"]")
@Since("2.2-dev36")
public class ExprFilterInput<T> extends SimpleExpression<T> {

	static {
		Skript.registerExpression(ExprFilterInput.class, Object.class, ExpressionType.COMBINED,
			"input",
			"%*classinfo% input"
		);
	}

	@Nullable
	private final ExprFilterInput<?> source;
	private final Class<? extends T>[] types;
	private final Class<T> superType;

	private ExprFilter parentFilter;

	@Nullable
	private ClassInfo<?> specifiedType;

	public ExprFilterInput() {
		this(null, (Class<? extends T>) Object.class);
	}

	public ExprFilterInput(@Nullable ExprFilterInput<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			specifiedType = source.specifiedType;
			parentFilter = source.parentFilter;
			Set<ExprFilterInput<?>> dependentInputs = parentFilter.getDependentInputs();
			dependentInputs.remove(this.source);
			dependentInputs.add(this);

		}
		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		parentFilter = getParser().getData(ExprFilter.FilterData.class).getParentFilter();
		if (parentFilter == null)
			return false;
		specifiedType = matchedPattern == 0 ? null : ((Literal<ClassInfo<?>>) exprs[0]).getSingle();
		return true;
	}

	@Override
	protected T[] get(Event event) {
		Object currentValue = parentFilter.getCurrentFilterValue();
		if (currentValue == null || (specifiedType != null && !specifiedType.getC().isInstance(currentValue)))
			return (T[]) Array.newInstance(superType, 0);

		try {
			return Converters.convert(new Object[]{currentValue}, types, superType);
		} catch (ClassCastException exception) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}

	@Override
	public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		return new ExprFilterInput<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public Class<? extends T> getReturnType() {
		return superType;
	}

	@Nullable
	public ClassInfo<?> getSpecifiedType() {
		return specifiedType;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(Event event, boolean debug) {
		return specifiedType == null ? "input" : specifiedType.getCodeName() + " input";
	}

}
