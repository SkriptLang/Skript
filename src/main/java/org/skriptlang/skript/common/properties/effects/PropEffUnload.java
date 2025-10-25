package org.skriptlang.skript.common.properties.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseEffect;
import org.skriptlang.skript.lang.properties.PropertyHandler.EffectHandler;

@Name("Unload")
@Description("""
	Unloads something.
	""")
@Since("INSERT VERSION")
public class PropEffUnload extends PropertyBaseEffect<EffectHandler<?>> {

	static {
		register(PropEffUnload.class, "unload %objects%");
	}

	@Override
	public @NotNull Property<EffectHandler<?>> getProperty() {
		return Property.UNLOAD;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "unload " + expr.toString(event, debug);
	}

}
