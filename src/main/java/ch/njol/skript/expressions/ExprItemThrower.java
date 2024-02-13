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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Item Thrower")
@Description({
	"The entity which dropped an item, can be get, set, and delete.",
	"By setting the item thrower, this affects the trigger criteria for item pickups, such as advancements."
})
@Examples({
	"on drop:",
		"\tdelete the item thrower",
	"delete the item thrower of all dropped items"
})
@Since("INSERT VERSION")
public class ExprItemThrower extends PropertyExpression<Item, OfflinePlayer> {

	static {
		Skript.registerExpression(ExprItemThrower.class, OfflinePlayer.class, ExpressionType.PROPERTY,
			"[the] item thrower [of %itementities%]",
			"%itementities%'[s] item thrower"
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Item>) expressions[0]);
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event event, Item[] source) {
		if (source.length == 0)
			return new OfflinePlayer[0];
		List<OfflinePlayer> offlinePlayers = new ArrayList<>();
		for (Item item : source) {
			UUID uuid = item.getThrower();
			if (uuid == null)
				continue;
			OfflinePlayer owner = Bukkit.getPlayer(uuid);
			if (owner == null) {
				owner = Bukkit.getOfflinePlayer(uuid);
			}
			offlinePlayers.add(owner);
		}
		return offlinePlayers.toArray(new OfflinePlayer[0]);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET:
			case DELETE:
				return CollectionUtils.array(OfflinePlayer.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Item[] itemEntities = getExpr().getArray(event);
		if (itemEntities.length == 0)
			return;
		UUID owner = null;
		if (mode == ChangeMode.SET) {
			if (delta[0] instanceof OfflinePlayer) {
				owner = ((OfflinePlayer) delta[0]).getUniqueId();
			} else {
				return;
			}
		}
		for (Item item : itemEntities) {
			item.setThrower(owner);
		}
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "item thrower of " + getExpr().toString(event, debug);
	}

}
