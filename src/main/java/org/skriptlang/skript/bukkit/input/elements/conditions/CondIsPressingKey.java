package org.skriptlang.skript.bukkit.input.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.input.InputKey;

@Name("Is Pressing Key")
@Description("Checks if a player is pressing a certain input key.")
@Examples({
	"on player input:",
	"\tif player is pressing forward movement key:",
		"\t\tsend \"You are moving forward!\""
})
@Since("2.10")
@Keywords({"press", "input"})
@RequiredPlugins("Minecraft 1.21.2+")
public class CondIsPressingKey extends Condition {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerInputEvent")) {
			Skript.registerCondition(CondIsPressingKey.class,
				"%players% (is|are) pressing %inputkeys%",
				"%players% (isn't|is not|aren't|are not) pressing %inputkeys%",
				"%players% (was|were) pressing %inputkeys%",
				"%players% (wasn't|was not|weren't|were not) pressing %inputkeys%"
			);
		} else {
			Skript.registerCondition(CondIsPressingKey.class,
				"%players% (is|are) pressing %inputkeys%",
				"%players% (isn't|is not|aren't|are not) pressing %inputkeys%"
			);
		}
	}

	private Expression<Player> players;
	private Expression<InputKey> inputKeys;
	private boolean past;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) expressions[0];
		//noinspection unchecked
		inputKeys = (Expression<InputKey>) expressions[1];
		past = matchedPattern > 1;
		if (past && !getParser().isCurrentEvent(PlayerInputEvent.class))
			Skript.warning("Checking the past state of a player's input outside the 'player input' event has no effect.");
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Player eventPlayer = event instanceof PlayerInputEvent inputEvent ? inputEvent.getPlayer() : null;
		InputKey[] inputKeys = this.inputKeys.getAll(event);
		boolean and = this.inputKeys.getAnd();
		return players.check(event, player -> {
			Input input;
			// If we want to get the new input of the event-player, we must get it from the event
			if (!past && player.equals(eventPlayer)) {
				input = ((PlayerInputEvent) event).getInput();
			} else { // Otherwise, we get the current (or past in case of an event-player) input
				input = player.getCurrentInput();
			}
			return CollectionUtils.check(inputKeys, inputKey -> inputKey.check(input), and);
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(players);
		if (past) {
			builder.append(players.isSingle() ? "was" : "were");
		} else {
			builder.append(players.isSingle() ? "is" : "are");
		}
		if (isNegated())
			builder.append("not");
		builder.append("pressing");
		builder.append(inputKeys);
		return builder.toString();
	}

}
