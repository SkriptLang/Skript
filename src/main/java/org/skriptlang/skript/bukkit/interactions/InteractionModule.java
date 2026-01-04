package org.skriptlang.skript.bukkit.interactions;

import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.ChildAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.interactions.elements.effects.EffMakeResponsive;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprInteractionDimensions;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprLastInteractionDate;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprLastInteractionPlayer;

public class InteractionModule extends ChildAddonModule {

	/**
	 * Constructs a child addon module with the given parent module.
	 *
	 * @param parentModule The parent module that created this child module.
	 */
	public InteractionModule(AddonModule parentModule) {
		super(parentModule);
	}

	@Override
	public void load(SkriptAddon addon) {
		AddonModule.register(addon.syntaxRegistry(), origin(addon),
			CondIsResponsive::register,
			EffMakeResponsive::register,
			ExprInteractionDimensions::register,
			ExprLastInteractionDate::register,
			ExprLastInteractionPlayer::register);
	}

	public enum InteractionType {
		ATTACK,
		INTERACT,
		BOTH
	}

	/**
	 * Useful helper to get the latest {@link PreviousInteraction} of an {@link Interaction}.
	 * @param interaction The interaction entity to check.
	 * @return The most recent {@link PreviousInteraction}, or null if no interactions have occurred.
	 */
	public static @Nullable PreviousInteraction getLatestInteraction(Interaction interaction) {
		PreviousInteraction attack = interaction.getLastAttack();
		PreviousInteraction interact = interaction.getLastInteraction();
		if (attack == null) // no attacks, return last interact/null
			return interact;
		if (interact == null) // attack but no interact
			return attack;
		// both not null, compare
		if (attack.getTimestamp() > interact.getTimestamp())
			return attack;
		return interact;
	}

	@Override
	public String name() {
		return "interactions";
	}

}
