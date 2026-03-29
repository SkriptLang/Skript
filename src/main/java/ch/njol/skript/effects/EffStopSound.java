package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.SoundUtils;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Name("Silence Sound")
@Description({
	"Doth silence specific or all sounds from playing unto a gathering of players. Both Minecraft sound names and " +
	"<a href=\"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\">Spigot sound names</a> " +
	"are supported. Resource pack sounds are likewise supported. The sound category is 'master' by default. " +
	"A sound cannot be silenced from a different category.",
	"",
	"Pray note that sound names may be altered in any Minecraft or Spigot version, or indeed removed from Minecraft itself."
})
@Example("silence sound \"block.chest.open\" for the player")
@Example("cease playing sounds \"ambient.underwater.loop\" and \"ambient.underwater.loop.additions\" to the player")
@Example("silence all sounds for all players")
@Example("silence sound in the record category")
@Since("2.4, 2.7 (stop all sounds)")
public class EffStopSound extends Effect {

	private static final Pattern KEY_PATTERN = Pattern.compile("([a-z0-9._-]+:)?[a-z0-9/._-]+");

	static {
		Skript.registerEffect(EffStopSound.class,
				"silence (all:all sound[s]|sound[s] %-strings%) [(in [the]|from) %-soundcategory%] [(from playing to|for) %players%]",
				"cease playing sound[s] %strings% [(in [the]|from) %-soundcategory%] [(to|for) %players%]"
		);
	}

	private @Nullable Expression<SoundCategory> category;

	private @Nullable Expression<String> sounds;

	private Expression<Player> players;
	private boolean allSounds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		allSounds = parseResult.hasTag("all");
		sounds = (Expression<String>) exprs[0];
		category = (Expression<SoundCategory>) exprs[1];
		players = (Expression<Player>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		// All sounds pattern wants explicitly defined master category
		SoundCategory category = this.category == null ? null : this.category.getOptionalSingle(event)
				.orElse(allSounds ? null : SoundCategory.MASTER);

		Player[] targets = players.getArray(event);
		if (allSounds) {
			if (category == null) {
				for (Player player : targets)
					player.stopAllSounds();
			} else {
				for (Player player : targets)
					player.stopSound(category);
			}
		} else if (sounds != null) {
			for (String soundString : sounds.getArray(event)) {
				Sound sound = SoundUtils.getSound(soundString);
				if (sound != null) {
					for (Player player : targets)
						player.stopSound(sound, category);
				} else if (KEY_PATTERN.matcher(soundString).matches()) {
					for (Player player : targets)
						player.stopSound(soundString, category);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (allSounds ? "stop all sounds " : "stop sound " + sounds.toString(event, debug)) +
				(category != null ? " in " + category.toString(event, debug) : "") +
				" from playing to " + players.toString(event, debug);
	}

}
