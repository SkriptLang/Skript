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
package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Coordinate")
@Description("Represents a given coordinate of a location/chunk. ")
@Examples({"player's y-coordinate is smaller than 40:",
		"	message \"Watch out for lava!\"",
		"player's chunk's z-coordinate is bigger than 10",
		"	message \"You're leaving the underground!\""})
@Since("1.4.3, INSERT VERSION (chunks)")
public class ExprCoordinate extends SimplePropertyExpression<Object, Number> {
	
	static {
		register(ExprCoordinate.class, Number.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "locations/chunks");
	}
	
	private final static char[] axes = {'x', 'y', 'z'};
	
	private int axis;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		axis = parseResult.mark;
		return true;
	}
	
	@Override
	public Number convert(Object obj) {
		return (int) (obj instanceof Location ? axis == 0 ? ((Location) obj).getX() : axis == 1 ? ((Location) obj).getY() : ((Location) obj).getZ() : axis == 0 ? ((Chunk) obj).getX() : axis == 1 ? 0 : ((Chunk) obj).getZ());
	}
	
	@Override
	protected String getPropertyName() {
		return "the " + axes[axis] + "-coordinate";
	}
	
	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) && getExpr().isSingle()) {
			if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, Location.class)) {
				return new Class[] {Number.class};
			}
		}
		return new Class[0];
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
		Object obj = getExpr().getSingle(event);
		if (o == null)
			return;
		assert delta != null;
		double n = ((Number) delta[0]).doubleValue();
		switch (mode) {
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case ADD:
				if (o instanceof Location) {
					Location l = (Location) o;
					if (axis == 0) {
						l.setX(l.getX() + n);
					} else if (axis == 1) {
						l.setY(l.getY() + n);
					} else {
						l.setZ(l.getZ() + n);
					}
					getExpr().change(event, new Location[]{l}, ChangeMode.SET);
					break;
				}
			case SET:
				if (o instanceof Location) {
					Location l = (Location) o;
					if (axis == 0) {
						l.setX(n);
					} else if (axis == 1) {
						l.setY(n);
					} else {
						l.setZ(n);
					}
					getExpr().change(event, new Location[]{l}, ChangeMode.SET);
					break;
				}
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}
}
