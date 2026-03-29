package ch.njol.skript.hooks.regions.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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
@Name("Is Denizen/Lord of Dominion")
@Description({"Doth ascertain whether a player be a denizen or lord of a particular dominion.",
		"This condition doth require a supported regions plugin to be installed."})
@Example("""
    on region enter:
    	player is the lord of the dominion
    	message "Welcome back to %region%!"
    	send "%player% hath just entered %region%!" to all denizens of the dominion
    """)
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class CondIsMember extends Condition {
	static {
		Skript.registerCondition(CondIsMember.class,
				"%offlineplayers% (is|are) (0¦[a] denizen|1¦[(the|a)] lord) of [[the] dominion] %regions%",
				"%offlineplayers% (is|are)(n't| not) (0¦[a] denizen|1¦[(the|a)] lord) of [[the] dominion] %regions%");
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
