package org.skriptlang.skript.docs;

import org.jetbrains.annotations.Contract;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.docs.Origin.AddonOrigin;
import org.skriptlang.skript.docs.OriginImpl.UnknownOrigin;

/**
 * Provides information about the origin of something (such as syntax).
 */
public sealed interface Origin extends Documentable permits OriginImpl.UnknownOrigin, AddonOrigin {

	/**
	 * An origin to be used in cases where no information is known.
	 */
	Origin UNKNOWN = new UnknownOrigin();

	/**
	 * Constructs an origin from an addon.
	 * @param addon The addon to construct this origin from.
	 * @return An origin pointing to the provided addon.
	 */
	@Contract("_ -> new")
	static AddonOrigin of(SkriptAddon addon) {
		return new OriginImpl.AddonOriginImpl(addon);
	}

	/**
	 * Constructs an origin from an addon and a class.
	 * @param addon The addon to construct this origin from.
	 * @param clazz The class of the thing this origin is for.
	 * @return An origin pointing to the provided addon and class.
	 */
	@Contract("_, _ -> new")
	static ClassOrigin of(SkriptAddon addon, Class<?> clazz) {
		return new OriginImpl.ClassOriginImpl(addon, clazz);
	}

	/**
	 * An origin to be used for something provided by an addon.
	 * @see Origin#of(SkriptAddon)
	 */
	non-sealed interface AddonOrigin extends Origin {

		/**
		 * @return An unmodifiable view of the addon this origin describes.
		 * @see SkriptAddon#unmodifiableView()
		 */
		SkriptAddon addon();

		/**
		 * @return A string representing the name of the addon this origin describes.
		 * Equivalent to {@link SkriptAddon#name()}.
		 */
		@Override
		default String name() {
			return addon().name();
		}

	}

	/**
	 * An {@link AddonOrigin} that also provides information about the class of the thing this origin is for.
	 */
	interface ClassOrigin extends AddonOrigin {

		/**
		 * @return The class of the thing this origin describes.
		 */
		Class<?> originClass();

		@Override
		default void write(DocumentationAdapter adapter) {
			AddonOrigin.super.write(adapter);
			adapter.write("class", originClass().getName());
		}

	}

	/**
	 * @return A string representing this origin.
	 */
	String name();

	@Override
	default void write(DocumentationAdapter adapter) {
		adapter.write("name", name());
	}

}
