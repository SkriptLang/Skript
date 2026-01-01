package org.skriptlang.skript.common.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.properties.expressions.PropExprScale;
import org.skriptlang.skript.docs.Origin;

import java.io.IOException;

public class PropertiesModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		Origin origin = AddonModule.origin(addon, "type properties");
		PropExprScale.register(addon, origin);

		// fully use type properties for existing syntax
		// TODO: use registration API
		if (SkriptConfig.useTypeProperties.value()) {
			try {
				Skript.getAddonInstance().loadClasses("org.skriptlang.skript.common.properties", "expressions", "conditions");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
