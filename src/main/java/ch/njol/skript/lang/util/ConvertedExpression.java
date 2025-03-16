package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Represents a expression converted to another type. This, and not Expression, is the required return type of {@link SimpleExpression#getConvertedExpr(Class...)} because this
 * class
 * <ol>
 * <li>automatically lets the source expression handle everything apart from the get() methods</li>
 * <li>will never convert itself to another type, but rather request a new converted expression from the source expression.</li>
 * </ol>
 *
 * @author Peter Güttinger
 */
public class ConvertedExpression<F, T> implements Expression<T> {

	protected Expression<? extends F> source;
	protected Class<T> to;
	final Converter<? super F, ? extends T> converter;

	/**
	 * Converter information.
	 */
	private final Collection<ConverterInfo<? super F, ? extends T>> converterInfos;

	public ConvertedExpression(Expression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
		this.source = source;
		this.to = to;
		this.converter = info.getConverter();
		this.converterInfos = Collections.singleton(info);
	}

	/**
	 * @param source The expression to use for obtaining values
	 * @param to The type we are converting to
	 * @param infos A collection of converters to attempt
	 * @param performFromCheck Whether a safety check should be performed to ensure that objects being converted
	 *  are valid for the converter being attempted
	 */
	public ConvertedExpression(Expression<? extends F> source, Class<T> to, Collection<ConverterInfo<? super F, ? extends T>> infos, boolean performFromCheck) {
		this.source = source;
		this.to = to;
		this.converterInfos = infos;
		this.converter = fromObject -> {
			for (ConverterInfo<? super F, ? extends T> info : converterInfos) {
				if (!performFromCheck || info.getFrom().isInstance(fromObject)) { // the converter is safe to attempt
					T converted = info.getConverter().convert(fromObject);
					if (converted != null)
						return converted;
				}
			}
			return null;
		};
	}

	@SafeVarargs
	public static <F, T> @Nullable ConvertedExpression<F, T> newInstance(Expression<F> from, Class<T>... to) {
		assert !CollectionUtils.containsSuperclass(to, from.getReturnType());

		// we might be able to cast some (or all) of the possible return types to T
		// for possible return types that can't be directly cast, regular converters will be used
		List<ConverterInfo<? extends F, ? extends T>> infos = new ArrayList<>();
		for (Class<? extends F> type : from.possibleReturnTypes()) {
			if (CollectionUtils.containsSuperclass(to, type)) { // this type is of T, build a converter simply casting
				// noinspection unchecked - 'type' is a desired type in 'to'
				Class<T> toType = (Class<T>) type;
				infos.add(new ConverterInfo<>(type, toType, toType::cast, 0));
			} else { // this possible return type is not included in 'to'
				// build all converters for converting the possible return type into any of the types of 'to'
				for (Class<T> toType : to) {
					ConverterInfo<? extends F, T> converter = Converters.getConverterInfo(type, toType);
					if (converter != null)
						infos.add(converter);
				}
			}
		}
		if (!infos.isEmpty()) { // there are converters for (at least some of) the return types
			// a note: casting <? extends F> to <? super F> is wrong, but since the converter is used only for values
			//         returned by the expression (which are instances of <? extends F>), this won't result in any CCEs
			// noinspection rawtypes, unchecked
			return new ConvertedExpression(from, Utils.getSuperType(infos.stream().map(ConverterInfo::getTo).toArray(Class[]::new)), infos, true);
		}

		return null;
	}

	@Override
	public final boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult matcher) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (debug && event == null)
			return "(" + source.toString(event, debug) + " >> " + converter + ": "
				+ converterInfos.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
		return source.toString(event, debug);
	}

	@Override
	public String toString() {
		return toString(null, false);
	}

	@Override
	public Class<T> getReturnType() {
		return to;
	}

	@Override
	public boolean isSingle() {
		return source.isSingle();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> @Nullable Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, this.to))
			return (Expression<? extends R>) this;
		return source.getConvertedExpression(to);
	}

	private @Nullable ClassInfo<? super T> returnTypeInfo;

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		Class<?>[] validClasses = source.acceptChange(mode);
		if (validClasses == null) {
			ClassInfo<? super T> returnTypeInfo;
			this.returnTypeInfo = returnTypeInfo = Classes.getSuperClassInfo(getReturnType());
			Changer<?> changer = returnTypeInfo.getChanger();
			return changer == null ? null : changer.acceptChange(mode);
		}
		return validClasses;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ClassInfo<? super T> returnTypeInfo = this.returnTypeInfo;
		if (returnTypeInfo != null) {
			Changer<? super T> changer = returnTypeInfo.getChanger();
			if (changer != null)
				changer.change(getArray(event), delta, mode);
		} else {
			source.change(event, delta, mode);
		}
	}

	@Override
	public @Nullable T getSingle(Event event) {
		F value = source.getSingle(event);
		if (value == null)
			return null;
		return converter.convert(value);
	}

	@Override
	public T[] getArray(Event event) {
		return Converters.convert(source.getArray(event), to, converter);
	}

	@Override
	public T[] getAll(Event event) {
		return Converters.convert(source.getAll(event), to, converter);
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker, boolean negated) {
		return negated ^ check(event, checker);
	}

	@Override
	public boolean check(Event event, Checker<? super T> checker) {
		return source.check(event, (Checker<F>) value -> {
			T convertedValue = converter.convert(value);
			if (convertedValue == null) {
				return false;
			}
			return checker.check(convertedValue);
		});
	}

	@Override
	public boolean getAnd() {
		return source.getAnd();
	}

	@Override
	public boolean setTime(int time) {
		return source.setTime(time);
	}

	@Override
	public int getTime() {
		return source.getTime();
	}

	@Override
	public boolean isDefault() {
		return source.isDefault();
	}

	@Override
	public boolean isLoopOf(String input) {
		return false;// A loop does not convert the expression to loop
	}

	@Override
	public @Nullable Iterator<T> iterator(Event event) {
		Iterator<? extends F> iterator = source.iterator(event);
		if (iterator == null)
			return null;
		return new Iterator<>() {
			@Nullable T next = null;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;
				while (next == null && iterator.hasNext()) {
					F value = iterator.next();
					next = value == null ? null : converter.convert(value);
				}
				return next != null;
			}

			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				T n = next;
				next = null;
				assert n != null;
				return n;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Expression<?> getSource() {
		return source;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Expression<? extends T> simplify() {
		Expression<? extends T> convertedExpression = source.simplify().getConvertedExpression(to);
		if (convertedExpression != null)
			return convertedExpression;
		return this;
	}

	@Override
	public Object @Nullable [] beforeChange(Expression<?> changed, Object @Nullable [] delta) {
		return source.beforeChange(changed, delta); // Forward to source
		// TODO this is not entirely safe, even though probably works well enough
	}

}
