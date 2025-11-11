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

@Name("Disable")
@Description("""
	Disables something.
	""")
@Since("INSERT VERSION")
@RelatedProperty("disable")
public class PropEffDisable extends PropertyBaseEffect<EffectHandler<?>> {

	static {
		register(PropEffDisable.class, "disable %objects%");
	}

	@Override
	public @NotNull Property<EffectHandler<?>> getProperty() {
		return Property.DISABLE;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "disable " + expr.toString(event, debug);
	}

}
