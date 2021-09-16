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
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fishing Hook Apply Lure")
@Description("Returns whether the lure enchantment should be applied to reduce the wait time.")
@Examples({"on fish:",
			"\tset apply lure enchantment of fishing hook to true"})
@Events("fishing")
@Since("INSERT VERSION")
public class ExprFishingApplyLure extends SimpleExpression<Boolean> {

	static {
		Skript.registerExpression(ExprFishingApplyLure.class, Boolean.class, ExpressionType.SIMPLE,
			"apply lure [enchant[ment]] [of [fish[ing]] hook]",
			"[fish[ing]] hook'[s] apply lure [enchant[ment]]");
	}

	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing hook apply lure' expression can only be used in fish event.");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable Boolean[] get(Event e) {
		FishHook hook = ((PlayerFishEvent) e).getHook();
		return new Boolean[]{ hook.getApplyLure() };
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null || delta[0] == null) {
			return;
		}

		((PlayerFishEvent) e).getHook().setApplyLure((Boolean) delta[0]);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "apply lure of fishing hook";
	}
}
