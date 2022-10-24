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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Player Advancements")
@Description("The advancements of a player.")
@Examples({
	"add the advancement \"minecraft:adventure/root\" to advancements of player",
	"if advancements of player contains the advancement \"minecraft:adventure/root\"",
	"remove all advancements from advancements of player"
})
@Since("INSERT VERSION")
public class ExprAdvancementsOfPlayer extends SimpleExpression<Advancement> {

	static {
		Skript.registerExpression(ExprAdvancementsOfPlayer.class, Advancement.class, ExpressionType.SIMPLE,
			"[all] [[of] the] advancements (of|from) %players%",
			"[all [of]] %players%'[s] advancements");
	}

	private Expression<Player> players;
	private boolean single;

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the advancements of " + players.toString(e, debug);
	}

	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public Class<? extends Advancement> getReturnType() {
		return Advancement.class;
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case DELETE:
			case RESET:
				return CollectionUtils.array(Advancement[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		List<Advancement> advancements = new ArrayList<>();
		for (Player player : players.getArray(e)) {
			switch (mode) {
				case SET:
					assert delta != null;
					advancements.addAll(Arrays.asList((Advancement[]) delta));
					break;
				case ADD:
					assert delta != null;
					advancements.addAll(List.of(getAdvancementsFromPlayer(player)));
					advancements.addAll(Arrays.asList((Advancement[]) delta));
					break;
				case REMOVE:
					assert delta != null;
					advancements.addAll(List.of(getAdvancementsFromPlayer(player)));
					advancements.removeAll(Arrays.asList((Advancement[]) delta));
					break;
				case DELETE:
				case RESET:
					break;
			}
			AdvancementProgress progress;
			for (Advancement advancement : getAdvancementsFromPlayer(player)) {
				progress = player.getAdvancementProgress(advancement);
				for (String criteria : progress.getAwardedCriteria())
					progress.revokeCriteria(criteria);
			}
			for (Advancement advancement : advancements) {
				progress = player.getAdvancementProgress(advancement);
				for (String criteria : progress.getRemainingCriteria())
					progress.awardCriteria(criteria);
			}
		}
	}

	@Override
	protected @Nullable Advancement[] get(Event e) {
		for (Player player : players.getArray(e)) {
			single = getAdvancementsFromPlayer(player).length < 1;
			return getAdvancementsFromPlayer(player);
		}
		return null;
	}

	private static Advancement[] getAdvancementsFromPlayer(Player player) {
		List<Advancement> advancements = new ArrayList<>();
		for (Advancement advancement : Utils.getAllAdvancements())
			if (player.getAdvancementProgress(advancement).isDone())
				advancements.add(advancement);
		return advancements.toArray(new Advancement[0]);
	}
}
