package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;

@FunctionalInterface
public interface AnyOwner<T> extends AnyProvider {

	/**
	 * @return The owner of this.
	 */
	@UnknownNullability
	T getOwner();

	/**
	 * Sets the owner of this.
	 * @param value The new owner to set.
	 */
	default void setOwner(@Nullable Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This is called before {@link #setOwner(T)}.
	 * @implNote If true, {@link #getDisplayName()} and {@link #getAcceptedTypes()} must be implemented.
	 * @return True if this supports changing owner, otherwise false.
	 */
	default boolean supportsChangingOwner() {
		return false;
	}

	/**
	 * This is used within the runtime error as an identifier for the expected object.
	 * @return The displayed name of the ownable object.
	 */
	default String getDisplayName() {
		return null;
	}

	/**
	 * @return The accepted classes of an {@link AnyOwner} object.
	 */
	default Class<?>[] getAcceptedTypes() {
		return null;
	}

	/**
	 * @param provided the class of the provided delta object in a change method.
	 * @return The error message of the {@link AnyOwner} object, with explicit accepted types.
	 */
	default String getErrorMessage(Class<?> provided) {
		String acceptedTypes = StringUtils.join(
			Arrays.stream(getAcceptedTypes())
				.map(Classes::getSuperClassInfo)
				.map(ClassInfo::toString)
				.sorted() // Sorts alphabetically for predictability
				.map(Utils::a)
				.iterator()
			, ", ", " and ");
		return "The owner of " + Utils.a(getDisplayName()) + " cannot be set to " + Utils.a(Classes.getSuperClassInfo(provided).toString()) + " it can however be set to " + acceptedTypes + ".";
	}

	/**
	 * @param provided The class to check against.
	 * @return True if the provided class is an accepted type otherwise false.
	 */
	default boolean isAcceptedType(Class<?> provided) {
		return Arrays.stream(getAcceptedTypes()).anyMatch(c -> c.isAssignableFrom(provided));
	}

}
