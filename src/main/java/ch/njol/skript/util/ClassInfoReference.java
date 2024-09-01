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
package ch.njol.skript.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * A ClassInfoReference represents a specific reference to a classinfo including any derivable context
 */
public final class ClassInfoReference {

	@Nullable
	private static UnparsedLiteral getSourceUnparsedLiteral(Expression<?> expression) {
		while (!(expression instanceof UnparsedLiteral)) {
			Expression<?> nextEarliestExpression = expression.getSource();
			if (nextEarliestExpression == expression) {
				return null;
			}
			expression = nextEarliestExpression;
		}
		return (UnparsedLiteral) expression;
	}

	private static Kleenean determineIfPlural(Expression<ClassInfo<?>> classInfoExpression) {
		UnparsedLiteral sourceUnparsedLiteral = getSourceUnparsedLiteral(classInfoExpression);
		if (sourceUnparsedLiteral == null) {
			return Kleenean.UNKNOWN;
		}
		String originalExpr = sourceUnparsedLiteral.getData();
		boolean isPlural = Utils.getEnglishPlural(originalExpr).getSecond();
		return Kleenean.get(isPlural);
	}

	/**
	 * Wraps a ClassInfo expression as a ClassInfoReference expression which will return ClassInfoReferences
	 * with as much derivable context as possible
	 * @param classInfoExpression the ClassInfo expression to wrap
	 * @return a wrapper ClassInfoReference expression
	 */
	@NonNull
	public static Expression<ClassInfoReference> wrap(@NonNull Expression<ClassInfo<?>> classInfoExpression) {
		if (classInfoExpression instanceof ExpressionList) {
			ExpressionList<?> classInfoExpressionList = (ExpressionList<?>) classInfoExpression;
			Expression<ClassInfoReference>[] wrappedExpressions = Arrays.stream(classInfoExpressionList.getExpressions())
				.map(expression -> wrap((Expression<ClassInfo<?>>) expression))
				.toArray(Expression[]::new);
			return new ExpressionList<>(wrappedExpressions, ClassInfoReference.class, classInfoExpression.getAnd());
		}
		Kleenean isPlural = determineIfPlural(classInfoExpression);
		if (classInfoExpression instanceof Literal) {
			Literal<ClassInfo<?>> classInfoLiteral = (Literal<ClassInfo<?>>) classInfoExpression;
			ClassInfo<?> classInfo = classInfoLiteral.getSingle();
			return new SimpleLiteral<>(new ClassInfoReference(classInfo, isPlural), classInfoLiteral.isDefault());
		}
		return new SimpleExpression<ClassInfoReference>() {

			@Override
			@Nullable
			protected ClassInfoReference[] get(Event event) {
				if (classInfoExpression.isSingle()) {
					ClassInfo<?> classInfo = classInfoExpression.getSingle(event);
					if (classInfo == null) {
						return new ClassInfoReference[0];
					}
					return new ClassInfoReference[] { new ClassInfoReference(classInfo, isPlural) };
				}
				return classInfoExpression.stream(event)
					.map(ClassInfoReference::new)
					.toArray(ClassInfoReference[]::new);
			}

			@Override
			public boolean isSingle() {
				return classInfoExpression.isSingle();
			}

			@Override
			public Class<? extends ClassInfoReference> getReturnType() {
				return ClassInfoReference.class;
			}

			@Override
			public String toString(@Nullable Event event, boolean debug) {
				if (debug) {
					return classInfoExpression.toString(event, true) + "(wrapped by " + getClass().getSimpleName() + ")";
				}
				return classInfoExpression.toString(event, false);
			}

			@Override
			public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
				return classInfoExpression.init(expressions, matchedPattern, isDelayed, parseResult);
			}
		};
	}

	private Kleenean plural;
	private ClassInfo<?> classInfo;

	public ClassInfoReference(ClassInfo<?> classInfo) {
		this(classInfo, Kleenean.UNKNOWN);
	}

	public ClassInfoReference(ClassInfo<?> classInfo, Kleenean plural) {
		this.classInfo = classInfo;
		this.plural = plural;
	}

	/**
	 * @return A Kleenean representing whether this classinfo reference was plural. Kleeanan.UNKNOWN represents
	 * a reference which did not have appropriate context available.
	 */
	public Kleenean isPlural() {
		return plural;
	}

	public void setPlural(Kleenean plural) {
		this.plural = plural;
	}

	public ClassInfo<?> getClassInfo() {
		return classInfo;
	}

	public void setClassInfo(ClassInfo<?> classInfo) {
		this.classInfo = classInfo;
	}

}
