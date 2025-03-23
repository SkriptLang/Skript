package ch.njol.skript.lang.util.common;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public interface AnyUUID extends AnyProvider {

	/**
	 * @return This thing's {@link UUID}
	 */
	@UnknownNullability UUID uuid();

	/**
	 * This is called before {@link #setUUID(UUID)}.
	 * If the result is false, setting the uuid will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsUUIDChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's UUID, if possible.
	 * If not possible, then {@link #supportsUUIDChange()} should return false and this
	 * may throw an error.
	 *
	 * @param uuid The UUID to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setUUID(UUID uuid) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Whether this is an {@link OfflinePlayer}. This is used to know whether to try and catch an
	 * {@link UnsupportedOperationException} when trying to access the UUIDs of emulated
	 * offline players by plugins like ProtocolLib.
	 *
	 * @return whether this is an offline player
	 */
	default boolean isOfflinePlayer() {
		return false;
	}

}
