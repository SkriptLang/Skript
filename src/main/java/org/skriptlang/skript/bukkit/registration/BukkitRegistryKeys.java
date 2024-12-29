package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.SkriptEvent;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;

/**
 * A class containing {@link SyntaxRegistry} keys for Bukkit-specific syntax elements.
 */
@ApiStatus.Experimental
public final class BukkitRegistryKeys {

	private BukkitRegistryKeys() { }

	/**
	 * A key representing the Bukkit-specific {@link SkriptEvent} syntax element.
	 */
	public static final Key<Event<?>> EVENT = Key.of("event");

}
