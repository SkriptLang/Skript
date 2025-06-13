package ch.njol.skript.lang.util;

import ch.njol.skript.lang.KeyProviderExpression;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * A {@link ConvertedExpression} that converts a keyed expression to another type with consideration of keys.
 * This expression is used when the source expression is a {@link KeyProviderExpression}
 *
 * @see ConvertedExpression
 */
public class ConvertedKeyProviderExpression<F, T> extends ConvertedExpression<F, T> implements KeyProviderExpression<T>, KeyReceiverExpression<T> {

	private final WeakHashMap<Event, KeyedValues> arrayKeysCache = new WeakHashMap<>();
	private final WeakHashMap<Event, KeyedValues> allKeysCache = new WeakHashMap<>();

	public ConvertedKeyProviderExpression(KeyProviderExpression<? extends F> source, Class<T> to, ConverterInfo<? super F, ? extends T> info) {
		super(source, to, info);
	}

	public ConvertedKeyProviderExpression(KeyProviderExpression<? extends F> source, Class<T> to, Collection<ConverterInfo<? super F, ? extends T>> converterInfos, boolean performFromCheck) {
		super(source, to, converterInfos, performFromCheck);
	}

	@Override
	public T[] getArray(Event event) {
		return get(getSource().getArray(event), getSource().getArrayKeys(event), keys -> arrayKeysCache.put(event, keys));
	}

	@Override
	public T[] getAll(Event event) {
		return get(getSource().getAll(event), getSource().getAllKeys(event), keys -> allKeysCache.put(event, keys));
	}

	private T[] get(F[] source, String[] keys, Consumer<KeyedValues> convertedKeysConsumer) {
		//noinspection unchecked
		T[] converted = (T[]) Array.newInstance(to, source.length);
		Converters.convert(source, converted, converter);
		convertedKeysConsumer.accept(new KeyedValues(converted, keys));
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
		return arrayKeysCache.remove(event).keys();
	}

	@Override
	public @NotNull String @NotNull [] getAllKeys(Event event) {
		if (!allKeysCache.containsKey(event))
			throw new IllegalStateException();
		return allKeysCache.remove(event).keys();
	}

	@Override
	public boolean canReturnKeys() {
		return getSource().canReturnKeys();
	}

	@Override
	public boolean areKeysRecommended() {
		return getSource().areKeysRecommended();
	}

	private record KeyedValues(@Nullable Object[] values, String[] keys) {

		@Override
		public String[] keys() {
			for (int i = 0; i < values.length; i++)
				keys[i] = values[i] != null ? keys[i] : null;
			return ArrayUtils.removeAllOccurrences(keys, null);
		}

	}

}
