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
package org.skriptlang.skript.bukkit.displays.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Name("Display Transformation Rotation")
@Description({
        "Returns or changes the transformation rotation of <a href='classes.html#display'>displays</a>.",
        "The left rotation is applied first, with the right rotation then being applied based on the rotated axis."
})
@Examples("set left transformation rotation of last spawned block display to quaternion(1, 0, 0, 0) # reset block display")
@RequiredPlugins("Spigot 1.19.4+")
@Since("INSERT VERSION")
public class ExprDisplayTransformationRotation extends SimplePropertyExpression<Display, Quaternionf> {

	static {
		if (Skript.isRunningMinecraft(1, 19, 4))
			registerDefault(ExprDisplayTransformationRotation.class, Quaternionf.class, "(:left|right) [transformation] rotation", "displays");
	}

	private boolean left;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		left = parseResult.hasTag("left");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Quaternionf convert(Display display) {
		Transformation transformation = display.getTransformation();
		return left ? transformation.getLeftRotation() : transformation.getRightRotation();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Quaternionf.class);
		if (mode == ChangeMode.RESET)
			return CollectionUtils.array();
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Quaternionf quaternion = null;
		if (mode == ChangeMode.RESET)
			quaternion = new Quaternionf(0, 0, 0, 1);
		if (delta != null) {
			quaternion = (Quaternionf) delta[0];
		}
		if (quaternion == null || !quaternion.isFinite())
			return;
		for (Display display : getExpr().getArray(event)) {
			Transformation transformation = display.getTransformation();
			Transformation change;
			if (left) {
				change = new Transformation(transformation.getTranslation(), quaternion, transformation.getScale(), transformation.getRightRotation());
			} else {
				change = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), quaternion);
			}
			display.setTransformation(change);
		}
	}

	@Override
	public Class<? extends Quaternionf> getReturnType() {
		return Quaternionf.class;
	}

	@Override
	protected String getPropertyName() {
		return (left ? "left" : "right") + " transformation rotation";
	}

}
