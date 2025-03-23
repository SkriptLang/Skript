package ch.njol.skript.lang.util.common;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

public interface AnyIdentifier extends AnyProvider {

	/**
	 * @return This thing's {@link UUID}
	 */
	@UnknownNullability UUID identifier();

	/**
	 * This is called before {@link #setIdentifier(UUID)}.
	 * If the result is false, setting the identifier will never be attempted.
	 *
	 * @return Whether this supports being set
	 */
	default boolean supportsChange() {
		return false;
	}

	/**
	 * The behaviour for changing this thing's identifier, if possible.
	 * If not possible, then {@link #supportsChange()} should return false and this
	 * may throw an error.
	 *
	 * @param uuid The UUID to change
	 * @throws UnsupportedOperationException If this is impossible
	 */
	default void setIdentifier(UUID uuid) throws UnsupportedOperationException {
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
