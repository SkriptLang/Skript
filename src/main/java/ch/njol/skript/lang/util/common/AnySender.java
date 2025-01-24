package ch.njol.skript.lang.util.common;

import java.util.function.Supplier;

/**
 * A provider for something that can send 'messages'.
 * Anything implementing this (or convertible to this) can be used as a sender by
 * the {@link ch.njol.skript.effects.EffMessage} effect.
 * <p>
 * This should supply its actual self (in cases of conversion).
 *
 * @see AnyProvider
 */
@FunctionalInterface
public interface AnySender<Type> extends AnyProvider, Supplier<Type> {
}
