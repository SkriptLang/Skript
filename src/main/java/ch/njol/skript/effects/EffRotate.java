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
package ch.njol.skript.effects;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Rotate")
@Description("Rotate rotations or itemframes an amount of times based on the provided rotation.")
@Examples({
	"rotate the event-item frame clockwise 2 times",
	"rotate the event-item frame by 225 degrees"
})
@Since("INSERT VERSION")
public class EffRotate extends Effect {

	static {
		Skript.registerEffect(EffRotate.class, "rotate %~rotations% [:counter] clockwise %*number% times");
		Skript.registerEffect(EffRotate.class, "rotate %itemframes% [by] %rotation%");
	}

	@Nullable
	private Expression<ItemFrame> itemFrames;
	private Expression<Rotation> rotations;
	private boolean counter;
	private int amount;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		rotations = (Expression<Rotation>) exprs[matchedPattern ^ 0];
		if (matchedPattern == 0) {
			amount = Optional.ofNullable(((Literal<Number>) exprs[1]))
					.map(Literal::getSingle)
					.map(Number::intValue)
					.orElse(1);
		} else {
			itemFrames = (Expression<ItemFrame>) exprs[0];
		}
		counter = parseResult.hasTag("counter");
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (itemFrames != null) {
			Rotation rotation = rotations.getOptionalSingle(event).orElse(Rotation.NONE);
			if (rotation == Rotation.NONE)
				return;
			itemFrames.stream(event).forEach(itemFrame ->
					itemFrame.setRotation(rotate(itemFrame.getRotation(), rotation)));
			return;
		}
		rotations.change(event, rotations.stream(event).map(this::rotate).toArray(Rotation[]::new), ChangeMode.SET);
	}

	/**
	 * The amount of times to rotate clockwise to get to the matched degree.
	 */
	private static final Map<Rotation, Integer> order = new HashMap<>();

	static {
		order.put(Rotation.CLOCKWISE_45, 1);
		order.put(Rotation.CLOCKWISE, 2);
		order.put(Rotation.CLOCKWISE_135, 3);
		order.put(Rotation.FLIPPED, 4);
		order.put(Rotation.FLIPPED_45, 5);
		order.put(Rotation.COUNTER_CLOCKWISE, 6);
		order.put(Rotation.COUNTER_CLOCKWISE_45, 7);
	}

	private Rotation rotate(Rotation relative, Rotation rotation) {
		for (int i = 0; i < order.get(relative); i++)
			rotation.rotateClockwise();
		return rotation;
	}

	private Rotation rotate(Rotation rotation) {
		for (int i = 0; i < amount; i++) {
			if (counter) {
				rotation.rotateCounterClockwise();
			} else {
				rotation.rotateClockwise();
			}
		}
		return rotation;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String string;
		if (itemFrames != null) {
			string = "rotate " + itemFrames.toString(event, debug) + " " + rotations.toString(event, debug);
		} else {
			string = "rotate " + rotations.toString(event, debug) + (counter ? " counter " : "") + " clockwise";
		}
		return string + " " + amount + " time" + (amount <= 1 ? "" : "s");
	}

}
