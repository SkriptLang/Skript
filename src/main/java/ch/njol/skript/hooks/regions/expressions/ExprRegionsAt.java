/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;

/**
 * @author Peter Güttinger
 */
@Name("Regions At")
@Description({
	"All <a href='./classes.html#region'>regions</a> at a particular <a href='./classes/#location'>location</a>.",
	"This expression requires a supported regions plugin to be installed."
})
@Examples({
	"On click on a sign:",
	"\tline 1 of the clicked block is \"[region info]\"",
	"\tset {_regions::*} to regions at the clicked block",
	"\tif {_regions::*} is empty:",
	"\t\tmessage \"No regions exist at this sign.\"",
	"\telse:",
	"\t\tmessage \"Regions containing this sign: &lt;gold&gt;%{_regions::*}%<r>.\""
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprRegionsAt extends SimpleExpression<Region> {
	static {
		Skript.registerExpression(ExprRegionsAt.class, Region.class, ExpressionType.PROPERTY,
				"[the] region(1¦s|) %direction% %locations%");
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
