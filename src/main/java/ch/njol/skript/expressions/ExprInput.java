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
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.InputSource.InputData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.Set;

@Name("Input")
@Description({
	"Represents the input in a filter expression or sort effect.",
	"For example, if you ran 'broadcast \"something\" and \"something else\" where [input is \"something\"]",
	"the condition would be checked twice, using \"something\" and \"something else\" as the inputs.",
	"The 'input index' pattern can be used when acting on a variable to access the index of the input."
})
@Examples({
	"send \"congrats on being staff!\" to all players where [input has permission \"staff\"]",
	"sort {_list::*} based on length of input index"
})
@Since("2.2-dev36, 2.9.0 (input index)")
public class ExprInput<T> extends SimpleExpression<T> {

	static {
		Skript.registerExpression(ExprInput.class, Object.class, ExpressionType.COMBINED,
			"input",
			"%*classinfo% input",
			"input index"
		);
	}

	@Nullable
	private final ExprInput<?> source;
	private final Class<? extends T>[] types;
	private final Class<T> superType;

	private InputSource inputSource;

	@Nullable
	private ClassInfo<?> specifiedType;
	private boolean isIndex = false;

	public ExprInput() {
		this(null, (Class<? extends T>) Object.class);
	}

	public ExprInput(@Nullable ExprInput<?> source, Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			isIndex = source.isIndex;
			specifiedType = source.specifiedType;
			inputSource = source.inputSource;
			Set<ExprInput<?>> dependentInputs = inputSource.getDependentInputs();
			dependentInputs.remove(this.source);
			dependentInputs.add(this);
		}
		this.types = types;
		this.superType = (Class<T>) Utils.getSuperType(types);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		inputSource = getParser().getData(InputData.class).getSource();
		if (inputSource == null)
			return false;
		switch (matchedPattern) {
			case 1:
				ClassInfoReference classInfoReference = ((Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) exprs[0])).getSingle();
				if (classInfoReference.isPlural().isTrue()) {
					Skript.error("An input can only be a single value! Please use a singular type (for example: players input -> player input).");
					return false;
				}
				specifiedType = classInfoReference.getClassInfo();
				break;
			case 2:
				if (!inputSource.hasIndices()) {
					Skript.error("You cannot use 'input index' on lists without indices!");
					return false;
				}
				specifiedType = DefaultClasses.STRING;
				isIndex = true;
				break;
			default:
				specifiedType = null;
		}
		return true;
	}

	@Override
	protected T[] get(Event event) {
		Object currentValue = isIndex ? inputSource.getCurrentIndex() : inputSource.getCurrentValue();
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
		return new ExprInput<>(this, to);
	}

	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}

	@Override
	public boolean isSingle() {
		return true;
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
	public String toString(Event event, boolean debug) {
		if (isIndex)
			return "input index";
		return specifiedType == null ? "input" : specifiedType.getCodeName() + " input";
	}

}
