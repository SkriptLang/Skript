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
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Advancements of Player")
@Description("Returns the advancements of a player.")
@Examples({
	"add the advancement \"minecraft:adventure/root\" to advancements of player",
	"if advancements of player contains the advancement \"minecraft:adventure/root\"",
	"remove all advancements from advancements of player"
})
@Since("INSERT VERSION")
public class ExprAdvancementsOfPlayer extends SimpleExpression<Advancement> {

	static {
		Skript.registerExpression(ExprAdvancementsOfPlayer.class, Advancement.class, ExpressionType.SIMPLE,
			"[the] advancements of %player%",
			"%player%'[s] advancements"
		);
	}

	private Expression<Player> players;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Advancement[] get(Event event) {
		List<Advancement> advancementList = new ArrayList<>();
		for (Player player : players.getArray(event))
			advancementList.addAll(getAdvancementsFromPlayer(player));
		return advancementList.toArray(new Advancement[0]);
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
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		for (Player player : players.getArray(event)) {
			AdvancementProgress progress;
			switch (mode) {
				case SET:
					if (delta != null) {
						for (Advancement advancement : getAdvancementsFromPlayer(player)) {
							progress = player.getAdvancementProgress(advancement);
							for (String criteria : progress.getAwardedCriteria())
								progress.revokeCriteria(criteria);
						}
						for (Advancement advancement : (Advancement[]) delta) {
							progress = player.getAdvancementProgress(advancement);
							for (String criteria : progress.getRemainingCriteria())
								progress.awardCriteria(criteria);
						}
					}
					break;
				case ADD:
					if (delta != null) {
						for (Advancement advancement : (Advancement[]) delta) {
							progress = player.getAdvancementProgress(advancement);
							for (String criteria : progress.getRemainingCriteria())
								progress.awardCriteria(criteria);
						}
					}
					break;
				case REMOVE:
					if (delta != null) {
						for (Advancement advancement : (Advancement[]) delta) {
							progress = player.getAdvancementProgress(advancement);
							for (String criteria : progress.getAwardedCriteria())
								progress.revokeCriteria(criteria);
						}
					}
					break;
				case DELETE:
				case RESET:
					for (Advancement advancement : getAdvancementsFromPlayer(player)) {
						progress = player.getAdvancementProgress(advancement);
						for (String criteria : progress.getAwardedCriteria())
							progress.revokeCriteria(criteria);
					}
					break;
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Advancement> getReturnType() {
		return Advancement.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the advancements of " + players.toString(event, debug);
	}

	private static List<Advancement> getAdvancementsFromPlayer(Player player) {
		List<Advancement> advancements = new ArrayList<>();
		for (Advancement advancement : Utils.getAllAdvancements()) {
			if (player.getAdvancementProgress(advancement).isDone())
				advancements.add(advancement);
		}
		return advancements;
	}
}
