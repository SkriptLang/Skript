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

@Name("Enable")
@Description("""
	Enables something.
	""")
@Since("INSERT VERSION")
@RelatedProperty("enable")
public class PropEffEnable extends PropertyBaseEffect<EffectHandler<?>> {

	static {
		register(PropEffEnable.class, "enable %objects%");
	}

	@Override
	public @NotNull Property<EffectHandler<?>> getProperty() {
		return Property.ENABLE;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "enable " + expr.toString(event, debug);
	}

}
