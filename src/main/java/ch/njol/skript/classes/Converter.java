package ch.njol.skript.classes;

import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Converts data from type to another.
 * 
 * @param <F> The accepted type of objects to convert <b>f</b>rom
 * @param <T> The type to convert <b>t</b>o
 * @see Converters#registerConverter(Class, Class, Converter)
 * @deprecated Use {@link org.skriptlang.skript.lang.converter.Converter}
 */
@Deprecated
public interface Converter<F, T> {
	
	/**
	 * Disallow other converters from being chained to this.
	 */
	public final static int NO_LEFT_CHAINING = 1;
	
	/**
	 * Disallow chaining this with other converters.
	 */
	public final static int NO_RIGHT_CHAINING = 2;
	
	/**
	 * Disallow all chaining.
	 */
	public final static int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;
	
	public final static int NO_COMMAND_ARGUMENTS = 4;
	
	/**
	 * holds information about a converter
	 * 
	 * @author Peter Güttinger
	 * @param <F> same as in {@link Converter}
	 * @param <T> dito
	 */
	public final static class ConverterInfo<F, T> implements Debuggable {
		
		public final Class<F> from;
		public final Class<T> to;
		public final Converter<F, T> converter;
		public final int options;
		
		/**
		 * Chain of types this converter will go through from right to left.
		 * For normal converters, contains from at 0 and to at 1. For chained
		 * converters, chains of first and second converter are concatenated
		 * together.
		 */
		private final Class<?>[] chain;
		
		public ConverterInfo(Class<F> from, Class<T> to, Converter<F, T> converter, int options) {
			this.from = from;
			this.to = to;
			this.converter = converter;
			this.options = options;
			this.chain = new Class[] {from, to};
		}
		
		@SuppressWarnings("unchecked")
		public ConverterInfo(ConverterInfo<?,?> first, ConverterInfo<?,?> second, Converter<F, T> converter, int options) {
			this.from = (Class<F>) first.from;
			this.to = (Class<T>) second.to;
			this.converter = converter;
			this.options = options;
			this.chain = new Class[first.chain.length + second.chain.length];
			System.arraycopy(first.chain, 0, chain, 0, first.chain.length);
			System.arraycopy(second.chain, 0, chain, first.chain.length, second.chain.length);
		}

		@Override
		public String toString(@Nullable Event event, boolean debug) {
			if (debug) {
				String str = Arrays.stream(chain).map(c -> Classes.getExactClassName(c)).collect(Collectors.joining(" -> "));
				assert str != null;
				return str;
			}
			return Classes.getExactClassName(from) + " to " + Classes.getExactClassName(to);
		}
		
	}
	
	/**
	 * Converts an object from the given to the desired type.
	 * 
	 * @param f The object to convert.
	 * @return the converted object
	 */
	@Nullable
	public T convert(F f);
	
	public final static class ConverterUtils {
		
		public static <F, T> Converter<?, T> createInstanceofConverter(final ConverterInfo<F, T> conv) {
			return createInstanceofConverter(conv.from, conv.converter);
		}
		
		public static <F, T> Converter<?, T> createInstanceofConverter(final Class<F> from, final Converter<F, T> conv) {
			return new Converter<Object, T>() {
				@SuppressWarnings("unchecked")
				@Override
				@Nullable
				public T convert(final Object o) {
					if (!from.isInstance(o))
						return null;
					return conv.convert((F) o);
				}
			};
		}
		
		/**
		 * Wraps a converter in a filter that will only accept conversion
		 * results of given type. All other results are replaced with nulls.
		 * @param conv Converter to wrap.
		 * @param to Accepted return type of the converter.
		 * @return The wrapped converter.
		 */
		public static <F, T> Converter<F, T> createInstanceofConverter(final Converter<F, ?> conv, final Class<T> to) {
			return new Converter<F, T>() {
				@SuppressWarnings("unchecked")
				@Override
				@Nullable
				public T convert(final F f) {
					final Object o = conv.convert(f);
					if (to.isInstance(o))
						return (T) o;
					return null;
				}
			};
		}
		
		public static <F, T> Converter<?, T> createDoubleInstanceofConverter(final ConverterInfo<F, ?> conv, final Class<T> to) {
			return createDoubleInstanceofConverter(conv.from, conv.converter, to);
		}
		
		/**
		 * Wraps a converter. When values given to the wrapper converter are
		 * not of accepted type, it will not be called; instead, a null is
		 * returned. When it returns a value that is not of accepted type, the
		 * wrapped converter will return null instead.
		 * @param from Accepted type of input.
		 * @param conv Converter to wrap.
		 * @param to Accepted type of output.
		 * @return A wrapped converter.
		 */
		public static <F, T> Converter<?, T> createDoubleInstanceofConverter(final Class<F> from, final Converter<F, ?> conv, final Class<T> to) {
			return new Converter<Object, T>() {
				@SuppressWarnings("unchecked")
				@Override
				@Nullable
				public T convert(final Object o) {
					if (!from.isInstance(o))
						return null;
					final Object o2 = conv.convert((F) o);
					if (to.isInstance(o2))
						return (T) o2;
					return null;
				}
			};
		}
		
	}
}
