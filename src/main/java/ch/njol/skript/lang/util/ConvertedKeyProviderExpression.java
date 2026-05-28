package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyReceiverExpression;
import ch.njol.skript.lang.KeyedValue;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

/**
 * A {@link ConvertedExpression} that converts a keyed expression to another type with consideration of keys.
 * This expression is used when the source expression is a {@link KeyProviderExpression}
 *
 * @see ConvertedExpression
 */
public class ConvertedKeyProviderExpression<F, T> extends ConvertedExpression<F, T> implements KeyProviderExpression<T>, KeyReceiverExpression<T> {

	private final WeakHashMap<Event, String[]> arrayKeysCache = new WeakHashMap<>();
	private final WeakHashMap<Event, String[]> allKeysCache = new WeakHashMap<>();
	private final boolean supportsKeyedChange;

	public ConvertedKeyProviderExpression(KeyProviderExpression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
		super(source, to, info);
		this.supportsKeyedChange = source instanceof KeyReceiverExpression<?>;
	}

	public ConvertedKeyProviderExpression(KeyProviderExpression<? extends F> source, Class<T>[] toExact, Collection<ConverterInfo<? super F, ? extends T>> converterInfos, boolean performFromCheck) {
		super(source, toExact, converterInfos, performFromCheck);
		this.supportsKeyedChange = source instanceof KeyReceiverExpression<?>;
	}

	@Override
	public T[] getArray(Event event) {
		if (!canReturnKeys()) {
			return super.getArray(event);
		}
		return get(getSource().getArray(event), getSource().getArrayKeys(event), keys -> arrayKeysCache.put(event, keys));
	}

	@Override
	public T[] getAll(Event event) {
		if (!canReturnKeys()) {
			return super.getAll(event);
		}
		return get(getSource().getAll(event), getSource().getAllKeys(event), keys -> allKeysCache.put(event, keys));
	}

	private T[] get(F[] source, String[] keys, Consumer<String[]> convertedKeysConsumer) {
		//noinspection unchecked
		T[] converted = (T[]) Array.newInstance(to, source.length);
		Converters.convert(source, converted, converter);
		for (int i = 0; i < converted.length; i++)
			keys[i] = converted[i] != null ? keys[i] : null;
		convertedKeysConsumer.accept(ArrayUtils.removeAllOccurrences(keys, null));
		converted = ArrayUtils.removeAllOccurrences(converted, null);
		return converted;
	}

	@Override
	public KeyProviderExpression<? extends F> getSource() {
		return (KeyProviderExpression<? extends F>) super.getSource();
	}

	@Override
	public @NotNull String @NotNull [] getArrayKeys(Event event) throws IllegalStateException {
		if (!arrayKeysCache.containsKey(event))
			throw new IllegalStateException();
		return arrayKeysCache.remove(event);
	}

	@Override
	public @NotNull String @NotNull [] getAllKeys(Event event) {
		if (!allKeysCache.containsKey(event))
			throw new IllegalStateException();
		return allKeysCache.remove(event);
	}

	@Override
	public boolean canReturnKeys() {
		return getSource().canReturnKeys();
	}

	@Override
	public boolean areKeysRecommended() {
		return getSource().areKeysRecommended();
	}

	@Override
	public void change(Event event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys) {
		if (supportsKeyedChange) {
			((KeyReceiverExpression<?>) getSource()).change(event, delta, mode, keys);
		} else {
			getSource().change(event, delta, mode);
		}
	}

	@Override
	public boolean isIndexLoop(String input) {
		return getSource().isIndexLoop(input);
	}

	@Override
	public boolean isLoopOf(String input) {
		return KeyProviderExpression.super.isLoopOf(input);
	}

	@Override
	public Iterator<KeyedValue<T>> keyedIterator(Event event) {
		Iterator<? extends KeyedValue<? extends F>> source = getSource().keyedIterator(event);
		return Iterators.filter(
			Iterators.transform(source, keyedValue -> {
				assert keyedValue != null;
				T convertedValue = converter.convert(keyedValue.value());
				return convertedValue != null ? keyedValue.withValue(convertedValue) : null;
			}),
			Objects::nonNull
		);
	}

}
