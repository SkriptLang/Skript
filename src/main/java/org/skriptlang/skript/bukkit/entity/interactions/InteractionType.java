package org.skriptlang.skript.bukkit.entity.interactions;

import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.interactions.elements.expressions.ExprLastInteractionDate;
import org.skriptlang.skript.bukkit.entity.interactions.elements.expressions.ExprLastInteractionPlayer;

/**
 * Helper enum to determine whether we should be looking for the last attack or last interaction.
 * @see ExprLastInteractionDate
 * @see ExprLastInteractionPlayer
 */
public enum InteractionType {
	ATTACK,
	INTERACT,
	BOTH;

	/**
	 * Useful helper to get the latest {@link Interaction.PreviousInteraction} of an {@link Interaction}.
	 *
	 * @param interaction The interaction entity to check.
	 * @return The most recent {@link Interaction.PreviousInteraction}, or null if no interactions have occurred.
	 */
	public static @Nullable Interaction.PreviousInteraction getLatest(Interaction interaction) {
		Interaction.PreviousInteraction attack = interaction.getLastAttack();
		Interaction.PreviousInteraction interact = interaction.getLastInteraction();
		if (attack == null) // no attacks, return last interact/null
			return interact;
		if (interact == null) // attack but no interact
			return attack;
		// both not null, compare
		if (attack.getTimestamp() > interact.getTimestamp())
			return attack;
		return interact;
	}

}
