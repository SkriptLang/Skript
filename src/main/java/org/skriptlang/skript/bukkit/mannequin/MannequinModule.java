package org.skriptlang.skript.bukkit.mannequin;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.mannequin.elements.CondMannequinImmovable;
import org.skriptlang.skript.bukkit.mannequin.elements.CondMannequinParts;
import org.skriptlang.skript.bukkit.mannequin.elements.EffMannequinImmovable;
import org.skriptlang.skript.bukkit.mannequin.elements.EffMannequinParts;
import org.skriptlang.skript.bukkit.mannequin.elements.ExprMannequinDesc;
import org.skriptlang.skript.bukkit.mannequin.elements.ExprMannequinSkin;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class MannequinModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.entity.Mannequin");
	}

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();

		CondMannequinParts.register(registry);
		CondMannequinImmovable.register(registry);
		EffMannequinParts.register(registry);
		EffMannequinImmovable.register(registry);
		ExprMannequinDesc.register(registry);
		ExprMannequinSkin.register(registry);

		MannequinData.register();
	}

}
