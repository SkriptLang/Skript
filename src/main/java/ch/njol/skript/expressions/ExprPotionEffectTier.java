/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Potion Effect Tier")
@Description("An expression to obtain the amplifier of a potion effect applied to an entity.")
@Examples("if the amplifier of haste of player >= 3:")
@Since("2.7")
public class ExprPotionEffectTier extends SimpleExpression<Integer> {

	static {
		Skript.registerExpression(ExprPotionEffectTier.class, Integer.class, ExpressionType.COMBINED,
				"[the] [potion] (tier|amplifier|level) of %potioneffecttypes% (of|for|on) %livingentities%"
		);
	}

	private Expression<PotionEffectType> typeExpr;
	private Expression<LivingEntity> entityExpr;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		typeExpr = (Expression<PotionEffectType>) exprs[0];
		entityExpr = (Expression<LivingEntity>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected Integer[] get(Event event) {
		PotionEffectType[] types = typeExpr.getArray(event);
		LivingEntity[] entities = entityExpr.getArray(event);
		List<Integer> result = new ArrayList<>();
		for (LivingEntity entity : entities) {
			for (PotionEffectType type : types) {
				PotionEffect effect = entity.getPotionEffect(type);
				result.add(effect == null ? 0 : effect.getAmplifier() + 1);
			}
		}
		return result.toArray(new Integer[0]);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "potion tier of " + typeExpr.toString(event, debug) + " of " + entityExpr.toString(event, debug);
	}

}
