package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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
@Name("Region Members & Owners")
@Description({
	"A list of members or owners of a <a href='./classes.html#region'>region</a>.",
	"This expression requires a supported regions plugin to be installed."
})
@Examples({
	"on entering of a region:",
	"\tmessage \"You're entering %region% whose owners are %owners of region%\""
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprMembersOfRegion extends SimpleExpression<OfflinePlayer> {
	static {
		Skript.registerExpression(ExprMembersOfRegion.class, OfflinePlayer.class, ExpressionType.PROPERTY,
				"(all|the|) (0¦members|1¦owner[s]) of [[the] region[s]] %regions%", "[[the] region[s]] %regions%'[s] (0¦members|1¦owner[s])");
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
