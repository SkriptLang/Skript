package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Has Played Before")
@Description("Checks whether a player has played on this server before. You can also use " +
	"<a href='events.html#first_join'>on first join</a> if you want to make triggers for new players.")
@Examples({
	"player has played on this server before",
	"player hasn't played before"
})
@Since("1.4, 2.7 (multiple players)")
public class CondPlayedBefore extends Condition {
	
	static {
		Skript.registerCondition(CondPlayedBefore.class,
				"%offlineplayers% [(has|have|did)] [already] play[ed] [on (this|the) server] (before|already)",
				"%offlineplayers% (has not|hasn't|have not|haven't|did not|didn't) [(already|yet)] play[ed] [on (this|the) server] (before|already|yet)");
	}
	
	@SuppressWarnings("null")
	private Expression<OfflinePlayer> players;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<OfflinePlayer>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return players.check(e,
				OfflinePlayer::hasPlayedBefore,
				isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return players.toString(e, debug) + (isNegated() ? (players.isSingle() ? " hasn't" : " haven't") : (players.isSingle() ? " has" : " have"))
			+ " played on this server before";
	}
	
}
