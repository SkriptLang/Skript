package org.skriptlang.skript.bukkit.interactions;

import ch.njol.skript.Skript;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.IOException;

public class InteractionModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		try {
			Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.interactions", "elements");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

}
