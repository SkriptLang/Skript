package ch.njol.skript.lang;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.lang.structure.StructureInfo;

import ch.njol.skript.SkriptAPIException;
import org.skriptlang.skript.util.Priority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @param <E> the syntax element this info is for
 * @deprecated Use {@link SyntaxInfo} ({@link SyntaxInfo#builder(Class)}) instead.
 * Note that some syntax types have specific {@link SyntaxInfo} implementations that they require.
 */
@Deprecated(since = "2.14", forRemoval = true)
public class SyntaxElementInfo<E extends SyntaxElement> implements SyntaxInfo<E> {

	private final @Nullable SyntaxInfo<E> source;

	// todo: 2.9 make all fields private
	public final Class<E> elementClass;
	public final String[] patterns;
	public final String originClassPath;

	public SyntaxElementInfo(String[] patterns, Class<E> elementClass, String originClassPath) throws IllegalArgumentException {
		if (Modifier.isAbstract(elementClass.getModifiers()))
			throw new SkriptAPIException("Class " + elementClass.getName() + " is abstract");

		this.source = null;
		this.patterns = patterns;
		this.elementClass = elementClass;
		this.originClassPath = originClassPath;
		try {
			elementClass.getConstructor();
		} catch (final NoSuchMethodException e) {
			// throwing an Exception throws an (empty) ExceptionInInitializerError instead, thus an Error is used
			throw new Error(elementClass + " does not have a public nullary constructor", e);
		} catch (final SecurityException e) {
			throw new IllegalStateException("Skript cannot run properly because a security manager is blocking it!");
		}
	}

	@ApiStatus.Internal
	protected SyntaxElementInfo(SyntaxInfo<E> source) throws IllegalArgumentException {
		this.source = source;
		this.patterns = source.patterns().toArray(new String[0]);
		this.elementClass = source.type();
		this.originClassPath = source.origin().name();
	}

	/**
	 * Get the class that represents this element.
	 * @return The Class of the element
	 */
	public Class<E> getElementClass() {
		return elementClass;
	}

	/**
	 * Get the patterns of this syntax element.
	 * @return Array of Skript patterns for this element
	 */
	public String[] getPatterns() {
		return Arrays.copyOf(patterns, patterns.length);
	}

	/**
	 * Get the original classpath for this element.
	 * @return The original ClassPath for this element
	 */
	public String getOriginClassPath() {
		return originClassPath;
	}

	@ApiStatus.Internal
	@Contract("_ -> new")
	@SuppressWarnings("unchecked")
	public static <I extends SyntaxElementInfo<E>, E extends SyntaxElement> I fromModern(SyntaxInfo<? extends E> info) {
		if (info instanceof SyntaxElementInfo<? extends E> oldInfo) {
			return (I) oldInfo;
		} else if (info instanceof BukkitSyntaxInfos.Event<?> event) {
			return (I) new SkriptEventInfo<>(event);
		} else if (info instanceof SyntaxInfo.Structure<?> structure) {
			return (I) new StructureInfo<>(structure);
		} else if (info instanceof SyntaxInfo.Expression<?, ?> expression) {
			return (I) new ExpressionInfo<>(expression);
		}

		return (I) new SyntaxElementInfo<>(info);
	}

	// Registration API Compatibility

	@Override
	@ApiStatus.Internal
	public Builder<? extends Builder<?, E>, E> toBuilder() {
		// should not be called for this object
		throw new UnsupportedOperationException();
	}

	@Override
	@ApiStatus.Internal
	public Origin origin() {
		if (source != null)
			return source.origin();
		return Origin.UNKNOWN;
	}

	@Override
	@ApiStatus.Internal
	public Class<E> type() {
		return getElementClass();
	}

	@Override
	@ApiStatus.Internal
	public E instance() {
		if (source != null)
			return source.instance();

		try {
			return type().getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
				 NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@ApiStatus.Internal
	public @Unmodifiable Collection<String> patterns() {
		if (source != null)
			return source.patterns();
		return List.of(getPatterns());
	}

	@Override
	@ApiStatus.Internal
	public Priority priority() {
		if (source != null)
			source.priority();
		return SyntaxInfo.COMBINED;
	}

}
