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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Hatching Number")
@Description({
	"The number of entities that will be hatched in a Player Egg Throw event.",
	"Please note that no more than 127 entities can be hatched at once."
})
@Examples({
	"on player egg throw:",
		"\tset the hatching number to 10"
})
@Events("Egg Throw")
@Since("INSERT VERSION")
public class ExprHatchingNumber extends SimpleExpression<Byte> {

	static {
		Skript.registerExpression(ExprHatchingNumber.class, Byte.class, ExpressionType.SIMPLE,
				"[the] hatching number"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerEggThrowEvent.class)) {
			Skript.error("You can't use 'the hatching number' outside of a Player Egg Throw event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected Byte[] get(Event event) {
		if (!(event instanceof PlayerEggThrowEvent))
			return new Byte[0];
		return new Byte[]{((PlayerEggThrowEvent) event).getNumHatches()};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		//noinspection ConstantConditions
		if (!(event instanceof PlayerEggThrowEvent) || delta == null)
			return;

		assert delta[0] != null;

		int value = ((Number) delta[0]).intValue();
		if (mode != ChangeMode.SET) {
			if (mode == ChangeMode.REMOVE)
				value *= -1;
			value = ((int) ((PlayerEggThrowEvent) event).getNumHatches()) + value;
		}

		((PlayerEggThrowEvent) event).setNumHatches((byte) Math.min(Math.max(0, value), Byte.MAX_VALUE));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Byte> getReturnType() {
		return Byte.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the hatching number";
	}

}
