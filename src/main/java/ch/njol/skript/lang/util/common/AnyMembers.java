package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

/**
 * A provider for anything with members.
 * Anything implementing this (or convertible to this) can be used by the {@link ch.njol.skript.expressions.ExprMembers}
 * property expression.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyMembers<MemberType> extends AnyProvider, Iterable<MemberType> {

	/**
	 * This should return the set of (known) members.
	 * <br/>
	 * Ideally, this would be a (modifiable) backing set, which can be directly changed by
	 * {@link #setMembers(Collection)} if changers are enabled.
	 * That is not a requirement, but if implementations want to allow member changers, then
	 * the default changing methods must be overridden.
	 *
	 * @return This thing's set of members
	 */
	Collection<MemberType> members();

	/**
	 * @return A helper iterator for the member set
	 */
	@Override
	default @NotNull Iterator<MemberType> iterator() {
		return this.members().iterator();
	}

	/**
	 * @param member The object to test
	 * @return Whether this object is a member of the member set
	 */
	default boolean isMember(@Nullable Object member) {
		return this.members().contains(member);
	}

	default boolean isSafeMemberType(@Nullable Object member) {
		try {
			//noinspection unchecked (this is the best we can do to catch it here)
			MemberType thing = (MemberType) member;
			return thing != null;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * @return Whether this supports changers (add, set, remove, reset)
	 */
	default boolean membersSupportChanges() {
		return false;
	}

	/**
	 * The behaviour for replacing this thing's set of members, if possible.
	 * If not possible, then {@link #membersSupportChanges()} should return false and this
	 * may throw an error.
	 * <br/><br/>
	 * <b>Note:</b> the default implementation of this assumes {@link #members()} returns
	 * the (modifiable) backing collection.
	 * If this is not the desired behaviour for changing, then this must be overridden.
	 *
	 * @param members The new set of members
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setMembers(Collection<?> members) throws UnsupportedOperationException {
		Collection<MemberType> ours = this.members();
		ours.clear();
		for (Object member : members) {
			if (isSafeMemberType(member)) //noinspection unchecked
				ours.add((MemberType) member);
		}
	}

	/**
	 * The behaviour for adding to this thing's set of members, if possible.
	 * If not possible, then {@link #membersSupportChanges()} should return false and this
	 * may throw an error.
	 * <br/><br/>
	 * <b>Note:</b> the default implementation of this assumes {@link #members()} returns
	 * the (modifiable) backing collection.
	 * If this is not the desired behaviour for changing, then this must be overridden.
	 *
	 * @param members A set of members to add
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void addMembers(Collection<?> members) throws UnsupportedOperationException {
		Collection<MemberType> ours = this.members();
		for (Object member : members) {
			if (isSafeMemberType(member)) //noinspection unchecked
				ours.add((MemberType) member);
		}
	}

	/**
	 * The behaviour for removing from this thing's set of members, if possible.
	 * If not possible, then {@link #membersSupportChanges()} should return false and this
	 * may throw an error.
	 * <br/><br/>
	 * <b>Note:</b> the default implementation of this assumes {@link #members()} returns
	 * the (modifiable) backing collection.
	 * If this is not the desired behaviour for changing, then this must be overridden.
	 *
	 * @param members A set of members to remove
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void removeMembers(Collection<?> members) throws UnsupportedOperationException {
		this.members().removeAll(members);
	}

	/**
	 * The behaviour for resetting this thing's set of members, if possible.
	 * If not possible, then {@link #membersSupportChanges()} should return false and this
	 * may throw an error.
	 * <br/><br/>
	 * <b>Note:</b> the default implementation of this assumes {@link #members()} returns
	 * the (modifiable) backing collection.
	 * If this is not the desired behaviour for changing, then this must be overridden.
	 *
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void resetMembers() throws UnsupportedOperationException {
		this.members().clear();
	}

}
