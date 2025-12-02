package org.skriptlang.skript.bukkit.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumParser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

/**
 * A class to hold metadata about {@link org.bukkit.Effect}s before playing.
 */
public class GameEffect {

	public static final EnumParser<Effect> ENUM_UTILS = new EnumParser<>(Effect.class, "game effect"); // exclude effects that require data

	/**
	 * The {@link Effect} that this object represents
	 */
	private final Effect effect;

	/**
	 * The optional extra data that some {@link Effect}s require.
	 */
	private @Nullable Object data;

	/**
	 * Creates a new GameEffect with the given effect.
	 * @param effect the effect
	 */
	public GameEffect(Effect effect) {
		this.effect = effect;
	}

	/**
	 * Parses a GameEffect from the given input string. Prints errors if the parsed effect requires data.
	 * @param input the input string
	 * @return the parsed GameEffect, or null if the input is invalid
	 */
	public static GameEffect parse(String input) {
		Effect effect = ENUM_UTILS.parse(input.toLowerCase(Locale.ENGLISH), ParseContext.DEFAULT);
		if (effect == null)
			return null;
		if (effect.getData() != null) {
			Skript.error("The effect " + Classes.toString(effect) + " requires data and cannot be parsed directly. Use the Game Effect expression instead.");
			return null;
		}
		return new GameEffect(effect);
	}

	/**
	 * The backing {@link Effect}.
	 * @return the effect
	 */
	public Effect getEffect() {
		return effect;
	}

	/**
	 * The optional data for this effect.
	 * @return the data, or null if none is set (or not required)
	 */
	public @Nullable Object getData() {
		return data;
	}

	/**
	 * Sets the data for this effect. The data must be of the correct type for the effect.
	 * @param data the data to set. May only be null for the ELECTRIC_SPARK effect.
	 * @return true if the data was set correctly, false otherwise
	 */
	public boolean setData(Object data) {
		if (effect.getData() != null && effect.getData().isInstance(data)) {
			this.data = data;
			return true;
		} else if (effect == Effect.ELECTRIC_SPARK && data == null) {
			// ELECTRIC_SPARK effect can have null data
			this.data = null;
			return true;
		}
		return false;
	}

	/**
	 * Plays the effect at the given location. The given location must have a world.
	 * @param location the location to play the effect at
	 * @param radius the radius to play the effect in, or null to use the default radius
	 */
	public void draw(@NotNull Location location, @Nullable Number radius) {
		if (effect.getData() != null && data == null)
			return;
		World world = location.getWorld();
		if (world == null)
			return;
		if (radius == null) {
			location.getWorld().playEffect(location, effect, data);
		} else {
			location.getWorld().playEffect(location, effect, data, radius.intValue());
		}
	}

	/**
	 * Plays the effect for the given player.
	 * @param location the location to play the effect at
	 * @param player the player to play the effect for
	 */
	public void drawForPlayer(Location location, @NotNull Player player) {
		player.playEffect(location, effect, data);
	}

	public String toString(int flags) {
		return ENUM_UTILS.toString(getEffect(), flags);
	}

	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * A cached array of all effect names that do not require data.
	 */
	static final String[] namesWithoutData = Arrays.stream(Effect.values())
			.filter(effect -> effect.getData() == null)
			.map(Enum::name)
			.toArray(String[]::new);

	/**
	 * @return an array of all effect names that do not require data.
	 */
	public static String[] getAllNamesWithoutData(){
		return namesWithoutData.clone();
	}

}
