package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.doc.Example;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Title - Clear/Reset")
@Description({
	"Clears or resets the title of the player to the default values.",
	"While both actions remove the title being displayed, <code>reset</code> will also reset the title timings."
})
@Example("reset the titles of all players")
@Example("clear the title")
@Since("2.3, INSERT VERSION (clearing the title)")
public class EffResetTitle extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffResetTitle.class)
			.supplier(EffResetTitle::new)
			.addPatterns("(clear|delete|:reset) [the] title[s] [of %players%]",
				"(clear|delete|:reset) [the] %players%'[s] title[s]")
			.build());
	}

	private Expression<Player> players;
	private boolean reset;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) exprs[0];
		reset = parseResult.hasTag("reset");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] players = this.players.getArray(event);
		if (reset) {
			for (Player player : players) {
				player.resetTitle();
			}
		} else {
			for (Player player : players) {
				player.clearTitle();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (reset) {
			builder.append("reset");
		} else {
			builder.append("clear");
		}
		builder.append("the");
		if (players.isSingle()) {
			builder.append("title");
		} else {
			builder.append("titles");
		}
		builder.append("of", players);
		return builder.toString();
	}

}
