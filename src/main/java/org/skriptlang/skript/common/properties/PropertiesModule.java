package org.skriptlang.skript.common.properties;

import ch.njol.skript.SkriptConfig;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.properties.conditions.PropCondContains;
import org.skriptlang.skript.common.properties.conditions.PropCondIsEmpty;
import org.skriptlang.skript.common.properties.expressions.*;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class PropertiesModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();
		Origin origin = AddonModule.origin(addon, "type properties");
		PropExprScale.register(registry, origin);
		if (SkriptConfig.useTypeProperties.value()) { // not using canLoad since this should only gate old properties, not new ones
			PropCondContains.register(registry, origin);
			PropCondIsEmpty.register(registry, origin);

			PropExprAmount.register(registry, origin);
			PropExprCustomName.register(registry, origin);
			PropExprName.register(registry, origin);
			PropExprNumber.register(registry, origin);
			PropExprSize.register(registry, origin);
			PropExprValueOf.register(registry, origin);
			PropExprWXYZ.register(registry, origin);
		}
	}
}
