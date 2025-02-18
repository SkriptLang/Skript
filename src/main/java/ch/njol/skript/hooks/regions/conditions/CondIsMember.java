package ch.njol.skript.hooks.regions.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * @author Peter Güttinger
 */
@Name("Is Member/Owner of Region")
@Description({"Checks whether a player is a member or owner of a particular region.",
		"This condition requires a supported regions plugin to be installed."})
@Examples({
	"on region enter:",
	"\tplayer is the owner of the region",
	"\tmessage \"Welcome back to %region%!\"",
	"\tsend \"%player% just entered %region%!\" to all members of the region"
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class CondIsMember extends Condition {
	static {
		Skript.registerCondition(CondIsMember.class,
				"%offlineplayers% (is|are) (0¦[a] member|1¦[(the|an)] owner) of [[the] region] %regions%",
				"%offlineplayers% (is|are)(n't| not) (0¦[a] member|1¦[(the|an)] owner) of [[the] region] %regions%");
	}

	@SuppressWarnings("null")
	private Expression<OfflinePlayer> players;
	@SuppressWarnings("null")
	Expression<Region> regions;

	boolean owner;

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = (Expression<OfflinePlayer>) exprs[0];
		regions = (Expression<Region>) exprs[1];
		owner = parseResult.mark == 1;
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event,
			player -> regions.check(event,
				region -> owner ? region.isOwner(player) : region.isMember(player), isNegated()));
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return players.toString(e, debug) + " " + (players.isSingle() ? "is" : "are") + (isNegated() ? " not" : "") + " " + (owner ? "owner" : "member") + (players.isSingle() ? "" : "s") + " of " + regions.toString(e, debug);
	}
}
