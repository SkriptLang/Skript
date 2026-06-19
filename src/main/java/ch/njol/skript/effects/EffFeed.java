package ch.njol.skript.effects;

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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Feed")
@Description("Feeds the specified players.")
@Example("feed all players")
@Example("feed the player by 5 beefs")
@Since("2.2-dev34")
public class EffFeed extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffFeed.class)
			.addPatterns("feed %players% [by %-number% [beef[s]]]")
			.supplier(EffFeed::new)
			.build());
	}

	private Expression<Player> players;
	private @Nullable Expression<Number> beefs;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		beefs = (Expression<Number>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		int foodAmount = 0;

		if (beefs != null) {
			Number n = beefs.getSingle(event);
			if (n != null)
				foodAmount = n.intValue();
		}
		for (Player player : players.getArray(event)) {
			player.setFoodLevel(beefs == null ? 20 : player.getFoodLevel() + foodAmount);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "feed " + players.toString(e, debug) + (beefs != null ? " by " + beefs.toString(e, debug) : "");
	}


}
