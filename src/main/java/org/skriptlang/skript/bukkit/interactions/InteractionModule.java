package org.skriptlang.skript.bukkit.interactions;

import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.interactions.elements.effects.EffMakeResponsive;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprInteractionDimensions;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprLastInteractionDate;
import org.skriptlang.skript.bukkit.interactions.elements.expressions.ExprLastInteractionPlayer;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class InteractionModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		SyntaxRegistry registry = addon.syntaxRegistry();
		CondIsResponsive.register(registry);
		EffMakeResponsive.register(registry);
		ExprInteractionDimensions.register(registry);
		ExprLastInteractionDate.register(registry);
		ExprLastInteractionPlayer.register(registry);
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
		return "interaction";
	}

}
