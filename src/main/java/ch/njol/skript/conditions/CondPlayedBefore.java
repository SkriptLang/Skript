package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Hath Graced This Server Before")
@Description("Doth determine whether a player hath previously graced this server with their presence. Thou mayest also employ " +
	"<a href='#first_join'>on first join</a> shouldst thou wish to fashion triggers for newcomers.")
@Example("player has graced this server before")
@Example("player hasn't graced the server before")
@Since("1.4, 2.7 (multiple players)")
public class CondPlayedBefore extends Condition {
	
	static {
		Skript.registerCondition(CondPlayedBefore.class,
				"%offlineplayers% [(has|have|did)] [already] graced [the halls of] (this|the) server (before|already)",
				"%offlineplayers% (has not|hasn't|have not|haven't|did not|didn't) [(already|yet)] graced [the halls of] (this|the) server (before|already|yet)");
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
