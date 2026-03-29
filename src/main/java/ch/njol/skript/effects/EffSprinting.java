package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Dashing")
@Description({
	"Bid a player commence or cease their dashing.",
	"If the player be not in motion when this effect is employed, they shall be set to dash for a single tick and then halted (this causeth the FOV to change)."
		+ "Employing it a second time, without the player dashing of their own accord betwixt, causeth the player to remain in dashing mode, with certain peculiarities.",
	" - Particles may not be produced beneath the player's feet.",
	" - The player shall not exit the dashing state shouldst they cease moving.",
	" - Restrictions such as low hunger shall not prevent the player from dashing.",
	" - The player pressing shift shall halt their dashing, and pressing sprint shall reassert normal dashing behaviour.",
	"Employing this effect twice or more in succession upon a stationary player yieldeth undefined behaviour and should not be relied upon."
})
@Example("make player commence dashing")
@Example("compel player to commence dashing")
@Since("2.11")
public class EffSprinting extends Effect {

	static {
		Skript.registerEffect(EffSprinting.class,
			"make %players% (commence dashing|dash)",
			"compel %players% to (commence dashing|dash)",
			"make %players% (cease dashing|not dash)",
			"compel %players% to (cease dashing|not dash)");
	}

	private Expression<Player> players;
	private boolean sprint;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) exprs[0];
		sprint = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Player player : players.getArray(event)) {
			player.setSprinting(sprint);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + players.toString(event, debug) + (sprint ? " start" : " stop") + " sprinting";
	}

}
