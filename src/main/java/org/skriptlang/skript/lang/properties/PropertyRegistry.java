package org.skriptlang.skript.lang.properties;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.util.Registry;

import java.util.*;

/**
 * A registry handling {@link Property}s.
 * Skript and addons should register any new properties here to avoid conflicts.
 * If a conflict does occur, your property will fail to register and return false. You should check if your property
 * has a matching handler with the already registered one. If so, you should be able to use the other property instead of your own.
 */
@ApiStatus.Experimental
public class PropertyRegistry implements Registry<Property<?>> {

	private final Map<String, Property<?>> properties;
	private final Skript skript;

	public PropertyRegistry(Skript skript) {
		this.skript = skript;
		this.properties = new HashMap<>();
	}

	public boolean register(@NotNull Property<?> property) {
		String name = property.name();
		if (properties.containsKey(name)) {
			Skript.error("Property '" + name + "' is already registered by " + properties.get(name).provider().name() + ".");
			return false; // Property already registered
		}
		properties.put(name, property);
		Skript.debug("Registered property '" + name + "' provided by " + property.provider().name() + ".");
		return true;
	}

	public boolean unregister(@NotNull Property<?> property) {
		String name = property.name();
		return unregister(name);
	}

	public boolean unregister(String name) {
		name = name.toLowerCase(Locale.ENGLISH);
		if (!properties.containsKey(name)) {
			Skript.error("Property '" + name + "' is not registered and cannot be unregistered.");
			return false; // Property not registered
		}
		properties.remove(name);
		Skript.debug("Unregistered property '" + name + "'.");
		return true;
	}

	@Override
	public @Unmodifiable Collection<Property<?>> elements() {
		return Collections.unmodifiableCollection(properties.values());
	}

	public Property<?> get(String name) {
		return properties.get(name);
	}

	public boolean isRegistered(@NotNull Property<?> property) {
		return isRegistered(property.name());
	}

	public boolean isRegistered(@NotNull String name) {
		return properties.containsKey(name.toLowerCase(Locale.ENGLISH));
	}

}
