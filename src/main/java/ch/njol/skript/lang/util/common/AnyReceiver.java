package ch.njol.skript.lang.util.common;

import org.jetbrains.annotations.Nullable;

/**
 * A provider for anything that can receive 'messages'.
 * Anything implementing this (or convertible to this) can be used as a recipient by
 * the {@link ch.njol.skript.effects.EffMessage} effect.
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnyReceiver<Message, Sender> extends AnyProvider {

	/**
	 * The behaviour for sending something to this receiver.
	 * The type should be filtered (or converted) by {@link #sendSafely(Object, AnySender)} first, where possible.
	 *
	 * @param message The 'message' object to send
	 * @throws IllegalArgumentException if the message argument was unsupported
	 */
	void send(Message message) throws IllegalArgumentException;

	/**
	 * An optional alternative method for types that support some kind of message 'sender'
	 * object.
	 * @param message The 'message' object to send
	 * @param sender The sender of the message
	 * @throws IllegalArgumentException if the message argument was unsupported
	 */
	default void send(Message message, @Nullable Sender sender) throws IllegalArgumentException {
		this.send(message);
	}

	/**
	 * Implementations should override this to check whether something is a legal argument
	 * to {@link #send(Object)}.
	 *
	 * @param message The 'message' object
	 * @return Whether this is a safe input for {@link #send(Object)}
	 */
	default boolean isSafeMessageObject(@Nullable Object message) {
		return true;
	}

	/**
	 * Sends a message to this receiver, with the most safety checks possible.
	 * Messages that can't be sent safely should fail silently.
	 * <p>
	 * This can be overridden by implementations to perform conversion to a safe message type.
	 *
	 * @param message The 'message' object
	 */
	default void sendSafely(@Nullable Object message, @Nullable AnySender<?> sender) {
		if (!this.isSafeMessageObject(message))
			return;
		try {
			if (sender != null)
				//noinspection unchecked
				this.send((Message) message, (Sender) sender.get());
			else
				//noinspection unchecked
				this.send((Message) message);
		} catch (ClassCastException | IllegalArgumentException ignored) {
			// Ideally, this should be overridden and/or converted safely
			// Fail silently
		}
	}

}
