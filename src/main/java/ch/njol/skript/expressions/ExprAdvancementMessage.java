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
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Advancement Message")
@Description("The message of an advancement in the <a href=\\\"./events.html#advancement_complete\\\">advancement complete event</a>.")
@Examples("set advancement message to \"%player% completed an advancement!\"")
@Since("INSERT VERSION")
public class ExprAdvancementMessage extends SimpleExpression<String> {

	static {
		if (Skript.classExists("net.kyori.adventure.text.Component") && Skript.classExists("io.papermc.paper.advancement.AdvancementDisplay"))
			Skript.registerExpression(ExprAdvancementMessage.class, String.class, ExpressionType.SIMPLE, "[the] advancement message");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerAdvancementDoneEvent.class)) {
			Skript.error("The advancement message expression can only be used inside of the advancement complete event.");
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		if (event instanceof PlayerAdvancementDoneEvent) {
			PlayerAdvancementDoneEvent advEvent = (PlayerAdvancementDoneEvent) event;
			if (advEvent.message() != null)
				return new String[]{Bukkit.getUnsafe().legacyComponentSerializer().serialize(advEvent.message())};
		}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (event instanceof PlayerAdvancementDoneEvent) {
			PlayerAdvancementDoneEvent advEvent = (PlayerAdvancementDoneEvent) event;
			switch (mode) {
				case SET:
					if (delta != null)
						advEvent.message(Component.text((String) delta[0]));
					break;
				case DELETE:
					advEvent.message(null);
					break;
				case RESET:
					Advancement advancement = advEvent.getAdvancement();
					if (advancement.getDisplay() != null)
						advEvent.message(getAdvancementMessage(advancement, advEvent.getPlayer()));
					break;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the advancement message";
	}

	private static Component getAdvancementMessage(Advancement advancement, Player player) {
		boolean isChallenge = advancement.getDisplay().frame() == AdvancementDisplay.Frame.CHALLENGE;
		NamedTextColor color = (isChallenge ? NamedTextColor.DARK_PURPLE : NamedTextColor.GREEN);
		Component a = advancement.getDisplay().title().hoverEvent(HoverEvent.showText(advancement.getDisplay().description().color(color)));
		return Component.text(player.getDisplayName() + ((isChallenge) ? " has completed the challenge " : " has made the advancement "))
			.color(NamedTextColor.WHITE)
			.append(Component.text("[")
				.color(color))
			.append(a
				.color(color))
			.append(Component.text("]")
				.color(color));
	}
}
