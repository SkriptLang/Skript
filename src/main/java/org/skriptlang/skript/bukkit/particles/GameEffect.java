package org.skriptlang.skript.bukkit.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EnumUtils;
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

	public static final EnumUtils<Effect> ENUM_UTILS = new EnumUtils<>(Effect.class, "game effect"); // exclude effects that require data

	/**
	 * The {@link Effect} that this object represents
	 */
	private final Effect effect;

	/**
	 * The optional extra data that some {@link Effect}s require.
	 */
	@Nullable
	private Object data;

	public GameEffect(Effect effect) {
		this.effect = effect;
	}

	public static GameEffect parse(String input) {
		Effect effect = ENUM_UTILS.parse(input.toLowerCase(Locale.ENGLISH));
		if (effect == null)
			return null;
		if (effect.getData() != null) {
			Skript.error("The effect " + Classes.toString(effect) + " requires data and cannot be parsed directly. Use the Game Effect expression instead.");
			return null;
		}
		return new GameEffect(effect);
	}

	public Effect getEffect() {
		return effect;
	}

	@Nullable
	public Object getData() {
		return data;
	}

	public boolean setData(Object data) {
		if (effect.getData() != null && effect.getData().isInstance(data)) {
			this.data = data;
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
		if (effect.getData() != null)
			return ENUM_UTILS.toString(getEffect(), flags);
		return toString();
	}

	static final String[] namesWithoutData = Arrays.stream(Effect.values())
			.filter(effect -> effect.getData() == null)
			.map(Enum::name)
			.toArray(String[]::new);
	public static String[] getAllNamesWithoutData(){
		return namesWithoutData.clone();
	}



// TODO: add getters, setters, maybe builder class? Add spawn method.
}
