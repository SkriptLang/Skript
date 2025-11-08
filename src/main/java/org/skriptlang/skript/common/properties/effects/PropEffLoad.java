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

@Name("Load")
@Description("""
	Loads something.
	""")
@Since("INSERT VERSION")
@RelatedProperty("load")
public class PropEffLoad extends PropertyBaseEffect<EffectHandler<?>> {

	static {
		register(PropEffLoad.class, "load %objects%");
	}

	@Override
	public @NotNull Property<EffectHandler<?>> getProperty() {
		return Property.LOAD;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "load " + expr.toString(event, debug);
	}

}
