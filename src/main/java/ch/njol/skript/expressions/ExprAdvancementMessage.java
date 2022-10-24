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
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.eclipse.jdt.annotation.Nullable;

public class ExprAdvancementMessage extends EventValueExpression<String> {

	static {
		if (Skript.classExists("net.kyori.adventure.text.Component"))
			Skript.registerExpression(ExprAdvancementMessage.class, String.class, ExpressionType.SIMPLE, "[the] advancement message");
	}

	public ExprAdvancementMessage() {
		super(String.class);
	}

	@Override
	@Nullable
	protected String[] get(Event e) {
		PlayerAdvancementDoneEvent event = (PlayerAdvancementDoneEvent) e;
		return new String[]{Bukkit.getUnsafe().legacyComponentSerializer().serialize(event.message())};
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the advancement message";
	}

	@Override
	public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case DELETE:
			case RESET:
				return CollectionUtils.array(String.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		PlayerAdvancementDoneEvent event = (PlayerAdvancementDoneEvent) e;
		assert delta[0] != null;
		switch (mode) {
			case SET:
				event.message(Component.text((String) delta[0]));
				break;
			case DELETE:
			case RESET:
				event.message(Component.empty());
				break;
		}
	}
}
