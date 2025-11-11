package org.skriptlang.skript.common.properties.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseEffect;
import org.skriptlang.skript.lang.properties.PropertyHandler.EffectHandler;

@Name("Reload")
@Description("""
	Reloads something.
	""")
@Since("INSERT VERSION")
@RelatedProperty("reload")
public class PropEffReload extends PropertyBaseEffect<EffectHandler<?>> {

	static {
		register(PropEffReload.class, "reload %objects%");
	}

	@Override
	public @NotNull Property<EffectHandler<?>> getProperty() {
		return Property.RELOAD;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reload " + expr.toString(event, debug);
	}

}
