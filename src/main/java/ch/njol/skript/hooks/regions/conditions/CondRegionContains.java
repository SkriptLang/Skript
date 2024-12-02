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
import ch.njol.skript.util.Direction;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Region Contains")
@Description({
	"Checks whether a location is contained in a particular <a href='./classes.html#region'>region</a>.",
	"This condition requires a supported regions plugin to be installed."
})
@Examples({
	"player is in the region {regions::3}",
	"",
	"on region enter:",
	"\tregion contains {flags.%world%.red}",
	"\tmessage \"The red flag is near!\""
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class CondRegionContains extends Condition {
	static {
		Skript.registerCondition(CondRegionContains.class,
				"[[the] region] %regions% contain[s] %directions% %locations%", "%locations% (is|are) ([contained] in|part of) [[the] region] %regions%",
				"[[the] region] %regions% (do|does)(n't| not) contain %directions% %locations%", "%locations% (is|are)(n't| not) (contained in|part of) [[the] region] %regions%");
	}
	
	@SuppressWarnings("null")
	private Expression<Region> regions;
	@SuppressWarnings("null")
	Expression<Location> locs;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (exprs.length == 3) {
			regions = (Expression<Region>) exprs[0];
			locs = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		} else {
			regions = (Expression<Region>) exprs[1];
			locs = (Expression<Location>) exprs[0];
		}
		setNegated(matchedPattern >= 2);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return regions.check(e, new Checker<Region>() {
			@Override
			public boolean check(final Region r) {
				return locs.check(e, new Checker<Location>() {
					@Override
					public boolean check(final Location l) {
						return r.contains(l);
					}
				}, isNegated());
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return regions.toString(e, debug) + " contain" + (regions.isSingle() ? "s" : "") + " " + locs.toString(e, debug);
	}
	
}
