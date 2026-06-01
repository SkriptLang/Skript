package org.skriptlang.skript.bukkit.entity.interactions;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.entity.interactions.elements.effects.EffMakeResponsive;
import org.skriptlang.skript.bukkit.entity.interactions.elements.expressions.ExprInteractionDimensions;
import org.skriptlang.skript.bukkit.entity.interactions.elements.expressions.ExprLastInteractionDate;
import org.skriptlang.skript.bukkit.entity.interactions.elements.expressions.ExprLastInteractionPlayer;

public class InteractionModule extends HierarchicalAddonModule {

	/**
	 * Constructs a child addon module with the given parent module.
	 *
	 * @param parentModule The parent module that created this child module.
	 */
	public InteractionModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		register(addon,
			CondIsResponsive::register,
			EffMakeResponsive::register,
			ExprInteractionDimensions::register,
			ExprLastInteractionDate::register,
			ExprLastInteractionPlayer::register
		);
	}

	@Override
	public String name() {
		return "interaction";
	}

}
