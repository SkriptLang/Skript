package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.lang.structure.StructureInfo;

import ch.njol.skript.SkriptAPIException;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.util.ClassUtils;
import org.skriptlang.skript.util.Priority;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @param <E> the syntax element this info is for
 */
public class SyntaxElementInfo<E extends SyntaxElement> implements SyntaxInfo<E> {

	// todo: 2.9 make all fields private
	public final Class<E> elementClass;
	public final String[] patterns;
	public final String originClassPath;
	private Supplier<E> instanceSupplier;

	public SyntaxElementInfo(String[] patterns, Class<E> elementClass, String originClassPath) {
		this(patterns, elementClass, originClassPath, null);
	}

	public SyntaxElementInfo(String[] patterns, Class<E> elementClass, String originClassPath,
			@Nullable Supplier<E> instanceSupplier) throws IllegalArgumentException {
		if (Modifier.isAbstract(elementClass.getModifiers()))
			throw new SkriptAPIException("Class " + elementClass.getName() + " is abstract");
		this.patterns = patterns;
		this.elementClass = elementClass;
		this.originClassPath = originClassPath;
		this.instanceSupplier = instanceSupplier;
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

	@Contract("_ -> new")
	@ApiStatus.Internal
	@ApiStatus.Experimental
	@SuppressWarnings("unchecked")
	public static <I extends SyntaxElementInfo<E>, E extends SyntaxElement> I fromModern(SyntaxInfo<? extends E> info) {
		if (info instanceof SyntaxElementInfo<? extends E> oldInfo) {
			return (I) oldInfo;
		} else if (info instanceof BukkitSyntaxInfos.Event<?>) {
			BukkitSyntaxInfos.Event<SkriptEvent> event = (BukkitSyntaxInfos.Event<SkriptEvent>) info;
			// We must first go back to the raw input
			String rawName = event.name().startsWith("On ")
					? event.name().substring(3)
					: "*" + event.name();
			SkriptEventInfo<?> eventInfo = new SkriptEventInfo<>(
					rawName, event.patterns().toArray(new String[0]),
					event.type(), event.origin().name(),
					(Class<? extends Event>[]) event.events().toArray(new Class<?>[0]), event::instance);
			String documentationId = event.documentationId();
			if (documentationId != null)
				eventInfo.documentationID(documentationId);
			eventInfo.listeningBehavior(event.listeningBehavior())
					.since(event.since().toArray(new String[0]))
					.description(event.description().toArray(new String[0]))
					.examples(event.examples().toArray(new String[0]))
					.keywords(event.keywords().toArray(new String[0]))
					.requiredPlugins(event.requiredPlugins().toArray(new String[0]));
			return (I) eventInfo;
		} else if (info instanceof SyntaxInfo.Structure<?>) {
			var structure = (Structure<org.skriptlang.skript.lang.structure.Structure>) info;
			return (I) new StructureInfo<>(structure.patterns().toArray(new String[0]), structure.type(),
					structure.origin().name(), structure.entryValidator(), structure.nodeType(),
					structure::instance);
		} else if (info instanceof SyntaxInfo.Expression<?, ?> expression) {
			return (I) fromModernExpression(expression);
		}

		return (I) new SyntaxElementInfo<>(info.patterns().toArray(new String[0]), (Class<E>) info.type(), info.origin().name(),
			info::instance);
	}

	@Contract("_ -> new")
	@ApiStatus.Experimental
	private static <E extends ch.njol.skript.lang.Expression<R>, R> ExpressionInfo<E, R> fromModernExpression(
			SyntaxInfo.Expression<E, R> info) {
		return new ExpressionInfo<>(
				info.patterns().toArray(new String[0]), info.returnType(),
				info.type(), info.origin().name(), ExpressionType.fromModern(info.priority()),
				info::instance
		);
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
	public SyntaxOrigin origin() {
		return () -> originClassPath;
	}

	@Override
	@ApiStatus.Internal
	public Class<E> type() {
		return getElementClass();
	}

	@Override
	@ApiStatus.Internal
	public E instance() {
		if (instanceSupplier == null) {
			try {
				instanceSupplier = ClassUtils.instanceSupplier(getElementClass());
			} catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}
		return instanceSupplier.get();
	}

	@Override
	@ApiStatus.Internal
	public @Unmodifiable Collection<String> patterns() {
		return List.of(getPatterns());
	}

	@Override
	@ApiStatus.Internal
	public Priority priority() {
		return SyntaxInfo.COMBINED;
	}

}
