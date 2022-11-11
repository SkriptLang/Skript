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
package org.skriptlang.skript.lang.arithmetic;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.expressions.arithmetic.Operator;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains all registered arithmetics/differences and allows operating with them.
 */
@SuppressWarnings("unchecked")
public final class Arithmetics {

	private Arithmetics() {}

	private static final List<ArithmeticInfo<?>> registeredArithmetics = Collections.synchronizedList(new ArrayList<>());
	private static final List<DifferenceInfo<?, ?>> registeredDifferences = Collections.synchronizedList(new ArrayList<>());

	public static <T> void registerArithmetic(Class<T> type, Arithmetic<T> arithmetic) {
		if (getArithmeticInfoExact(type) != null)
			throw new SkriptAPIException("An arithmetic is already registered with this type");
		registeredArithmetics.add(new ArithmeticInfo<>(type, arithmetic));
	}

	@Nullable
	public static <T> Arithmetic<? super T> getArithmetic(Class<T> type) {
		ArithmeticInfo<? super T> arithmeticInfo = getArithmeticInfo(type);
		if (arithmeticInfo == null)
			return null;
		return arithmeticInfo.getArithmetic();
	}

	@Nullable
	public static <T> ArithmeticInfo<? super T> getArithmeticInfo(Class<T> type) {
		return (ArithmeticInfo<T>) registeredArithmetics.stream()
			.filter(info -> info.getType().isAssignableFrom(type))
			.findFirst().orElse(null);
	}

	@Nullable
	public static <T> ArithmeticInfo<T> getArithmeticInfoExact(Class<T> type) {
		return (ArithmeticInfo<T>) registeredArithmetics.stream()
			.filter(info -> info.getType() == type)
			.findFirst().orElse(null);
	}

	public static <T> T calculate(T first, Operator operator, Object second) {
		Arithmetic<T> arithmetic = (Arithmetic<T>) getArithmetic(first.getClass());
		if (arithmetic == null || !acceptsOperator(first.getClass(), operator, second.getClass()))
			return null;
		return arithmetic.calculate(first, operator, second);
	}

	public static <A, R> void registerDifference(Class<A> type, Class<R> relativeType, Difference<A, R> difference) {
		if (getDifferenceInfoExact(type) != null)
			throw new SkriptAPIException("A difference is already registered with this type");
		registeredDifferences.add(new DifferenceInfo<>(type, relativeType, difference));
	}

	public static <A> void registerDifference(Class<A> type, Difference<A, A> difference) {
		registerDifference(type, type, difference);
	}

	@Nullable
	public static <A> Difference<? super A, ?> getDifference(Class<A> type) {
		DifferenceInfo<? super A, ?> differenceInfo = getDifferenceInfo(type);
		if (differenceInfo == null)
			return null;
		return differenceInfo.getDifference();
	}

	@Nullable
	public static <A, R> Difference<? super A, ? super R> getDifference(Class<A> type, Class<R> relativeType) {
		DifferenceInfo<? super A, ?> differenceInfo = getDifferenceInfo(type);
		if (differenceInfo == null || !differenceInfo.getRelativeType().isAssignableFrom(relativeType))
			return null;
		return (Difference<? super A, ? super R>) differenceInfo.getDifference();
	}

	@Nullable
	public static <A> DifferenceInfo<? super A, ?> getDifferenceInfo(Class<A> type) {
		return (DifferenceInfo<? super A, ?>) registeredDifferences.stream()
			.filter(info -> info.getType().isAssignableFrom(type))
			.findFirst().orElse(null);
	}

	@Nullable
	public static <A> DifferenceInfo<A, ?> getDifferenceInfoExact(Class<A> type) {
		return (DifferenceInfo<A, ?>) registeredDifferences.stream()
			.filter(info -> info.getType() == type)
			.findFirst().orElse(null);
	}

	public static <A, R> R difference(A first, A second, Class<R> expectedReturnType) {
		Difference<A, R> difference = (Difference<A, R>) getDifference(first.getClass(), expectedReturnType);
		if (difference == null)
			return null;
		return difference.difference(first, second);
	}

	@Nullable
	public static <A> Object difference(A first, A second) {
		Difference<A, ?> difference = (Difference<A, ?>) getDifference(first.getClass());
		if (difference == null)
			return null;
		return difference.difference(first, second);
	}

	public static <T> boolean acceptsOperator(Class<T> c, Operator operator, Class<?>... types) {
		Arithmetic<? super T> arithmetic = getArithmetic(c);
		if (arithmetic == null)
			return false;
		return arithmetic.acceptsOperator(operator, types);
	}

}
