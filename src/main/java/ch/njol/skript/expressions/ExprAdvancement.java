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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Advancement")
@Description("A registered advancement.")
@Examples("remove the advancement \"minecraft:adventure/root\" from advancements of player")
@Since("INSERT VERSION")
public class ExprAdvancement extends SimpleExpression<Advancement> {

	static {
		Skript.registerExpression(ExprAdvancement.class, Advancement.class, ExpressionType.SIMPLE, "[the] advancement[s] %strings%");
	}

	private Expression<String> advancements;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		advancements = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	@Nullable
	protected Advancement[] get(Event event) {
		List<Advancement> advancementList = new ArrayList<>();
		for (String advancement : advancements.getArray(event)) {
			String namespace;
			String key;
			if (advancement.contains(":") && !advancement.startsWith(":")) {
				namespace = advancement.split(":")[0];
				key = advancement.split(":")[1];
			} else {
				namespace = "minecraft";
				key = advancement;
			}
			NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
			if (Bukkit.getAdvancement(namespacedKey) != null) {
				advancementList.add(Bukkit.getAdvancement(namespacedKey));
			}
		}
		return advancementList.toArray(new Advancement[0]);
	}

	@Override
	public boolean isSingle() {
		return advancements.isSingle();
	}

	@Override
	public Class<? extends Advancement> getReturnType() {
		return Advancement.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the advancements " + advancements.toString(event, debug);
	}
}
