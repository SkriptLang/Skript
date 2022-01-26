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

import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

@Name("Potion Effect")
@Description({"Create a new potion effect to apply to an entity or item type. Do note that when applying potion effects ",
	"to tipped arrows/lingering potions, Minecraft reduces the timespan."})
@Examples({"set {_p} to potion effect of speed of tier 1 without particles for 10 minutes",
	"add {_p} to potion effects of player's tool",
	"add {_p} to potion effects of target entity",
	"add potion effect of speed 1 to potion effects of player"})
@Since("2.5.2")
public class ExprPotionEffect extends SimpleExpression<PotionEffect> {
	static {
		Skript.registerExpression(ExprPotionEffect.class, PotionEffect.class, ExpressionType.COMBINED,
			"[new] potion effect of %potioneffecttype% [potion] [[[of] tier] %-number%] [(1¦without particles)] [for %-timespan%]",
			"[new] ambient potion effect of %potioneffecttype% [potion] [[[of] tier] %-number%] [(1¦without particles)] [for %-timespan%]");
	}
	
	@SuppressWarnings("null")
	private Expression<PotionEffectType> potionEffectType;
	@Nullable
	private Expression<Number> tier;
	@Nullable
	private Expression<Timespan> timespan;
	private boolean particles;
	private boolean ambient;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		potionEffectType = (Expression<PotionEffectType>) exprs[0];
		tier = (Expression<Number>) exprs[1];
		timespan = (Expression<Timespan>) exprs[2];
		particles = parseResult.mark == 0;
		ambient = matchedPattern == 1;
		return true;
	}
	
	@Override
	@Nullable
	protected PotionEffect[] get(final Event e) {
		PotionEffectType potionEffectType = this.potionEffectType.getSingle(e);
		if (potionEffectType == null)
			return null;
		int tier = 0;
		if (this.tier != null) {
			Number n = this.tier.getSingle(e);
			if (n != null)
				tier = n.intValue() - 1;
		}
		int ticks = 15 * 20; // 15 second default potion length
		if (this.timespan != null) {
			Timespan timespan = this.timespan.getSingle(e);
			if (timespan != null)
				ticks = (int) timespan.getTicks_i();
		}
		return new PotionEffect[]{new PotionEffect(potionEffectType, ticks, tier, ambient, particles)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends PotionEffect> getReturnType() {
		return PotionEffect.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		StringBuilder builder = new StringBuilder();
		if (ambient)
			builder.append("ambient ");
		builder.append("potion of ").append(potionEffectType.toString(e, debug));
		if (tier != null) {
			String t = tier.toString(e, debug);
			builder.append(" of tier/amp ").append(t);
		}
		if (!particles)
			builder.append(" without particles");
		builder.append(" for ");
		if (timespan != null)
			builder.append(timespan.toString(e, debug));
		else
			builder.append("15 seconds");
		return builder.toString();
	}
	
}
