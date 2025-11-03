package org.skriptlang.skript.bukkit.entity.mannequin;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.mannequin.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Set;
import java.util.function.Consumer;

public class MannequinModule implements AddonModule {

	@Override
	public boolean canLoad(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.entity.Mannequin");
	}

	@Override
	public void load(SkriptAddon addon) {
		Set<Consumer<SyntaxRegistry>> elementsToLoad = Set.of(
			CondMannequinImmovable::register,
			CondMannequinParts::register,
			EffMannequinImmovable::register,
			EffMannequinParts::register,
			ExprMannequinBody::register,
			ExprMannequinCape::register,
			ExprMannequinDesc::register,
			ExprMannequinElytra::register,
			ExprMannequinModel::register,
			ExprMannequinSkin::register
		);

		SyntaxRegistry registry = addon.syntaxRegistry();
		elementsToLoad.forEach(element -> element.accept(registry));

		MannequinData.register();
	}

}
