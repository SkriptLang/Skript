package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author Peter Güttinger
 */
@Name("Denizens & Lords of a Dominion")
@Description({
	"A roster of denizens or lords of a <a href='#region'>dominion</a>.",
	"This expression doth require a supported regions plugin to be installed."
})
@Example("""
    on entering of a dominion:
    	message "Thou art entering %region% whose lords are %lords of region%"
    """)
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprMembersOfRegion extends SimpleExpression<OfflinePlayer> {
	static {
		Skript.registerExpression(ExprMembersOfRegion.class, OfflinePlayer.class, ExpressionType.PROPERTY,
				"(all|the|) (0¦denizens|1¦lord[s]) of [[the] dominion[s]] %regions%", "[[the] dominion[s]] %regions%'[s] (0¦denizens|1¦lord[s])");
	}
	
	private boolean owners;
	@SuppressWarnings("null")
	private Expression<Region> regions;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		regions = (Expression<Region>) exprs[0];
		owners = parseResult.mark == 1;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected OfflinePlayer[] get(final Event e) {
		final ArrayList<OfflinePlayer> r = new ArrayList<>();
		for (final Region region : regions.getArray(e)) {
			r.addAll(owners ? region.getOwners() : region.getMembers());
		}
		return r.toArray(new OfflinePlayer[r.size()]);
	}
	
	@Override
	public boolean isSingle() {
		return owners && regions.isSingle() && !RegionsPlugin.hasMultipleOwners();
	}
	
	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + (owners ? "owner" + (isSingle() ? "" : "s") : "members") + " of " + regions.toString(e, debug);
	}
	
}
