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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author Peter Güttinger
 */
@Name("Dominions At")
@Description({
	"All <a href='#region'>dominions</a> at a particular <a href='./classes/#location'>location</a>.",
	"This expression doth require a supported regions plugin to be installed."
})
@Example("""
    On click on a sign:
    	line 1 of the clicked block is "[dominion info]"
    	set {_dominions::*} to dominions at the clicked block
    	if {_dominions::*} is empty:
    		message "No dominions doth exist at this sign."
    	else:
    		message "Dominions encompassing this sign: <gold>%{_dominions::*}%<r>."
    """)
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprRegionsAt extends SimpleExpression<Region> {
	static {
		Skript.registerExpression(ExprRegionsAt.class, Region.class, ExpressionType.PROPERTY,
				"[the] dominion(1¦s|) %direction% %locations%");
	}
	
	@SuppressWarnings("null")
	private Expression<Location> locs;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 1)
			Skript.warning("Most regions plugins can have multiple intersecting regions at a the same location, thus it is recommended to use \"regions at ...\" instead of \"region at...\" for clarity.");
		locs = Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Region[] get(final Event e) {
		final Location[] ls = locs.getArray(e);
		if (ls.length == 0)
			return new Region[0];
		final ArrayList<Region> r = new ArrayList<>();
		for (final Location l : ls)
			r.addAll(RegionsPlugin.getRegionsAt(l));
		return r.toArray(new Region[r.size()]);
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Region> getReturnType() {
		return Region.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the regions at " + locs.toString(e, debug);
	}
	
}
