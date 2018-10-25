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
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.expressions;

import org.bukkit.FireworkEffect;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class ExprFireworkEffect extends SimpleExpression<FireworkEffect> {

	static {
		Skript.registerExpression(ExprFireworkEffect.class, FireworkEffect.class, ExpressionType.COMBINED,
				"(1¦|2¦flickering|3¦trailing|4¦flickering trailing|5¦trailing flickering) %fireworktype% [firework [effect]] colo[u]red %colors%",
				"(1¦|2¦flickering|3¦trailing|4¦flickering trailing|5¦trailing flickering) %fireworktype% [firework [effect]] colo[u]red %colors% fad(e|ing) [to] %colors%");
	}
	
	@SuppressWarnings("null")
	private Expression<FireworkEffect.Type> type;
	@SuppressWarnings("null")
	private Expression<Color> color, fade;
	private boolean flicker, trail, hasFade;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		flicker = parseResult.mark == 2 && parseResult.mark > 3;
		trail = parseResult.mark >= 3;
		hasFade = matchedPattern == 1;
		type = (Expression<FireworkEffect.Type>) exprs[0];
		color = (Expression<Color>) exprs[1];
		if (hasFade)
			fade = (Expression<Color>) exprs[2];
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends FireworkEffect> getReturnType() {
		return FireworkEffect.class;
	}
	
	@Override
	@Nullable
	protected FireworkEffect[] get(Event e) {
		FireworkEffect.Type type = this.type.getSingle(e);
		if (type == null)
			return null;
		FireworkEffect.Builder builder = FireworkEffect.builder().with(type);
		
		for (Color colour : color.getArray(e))
			builder.withColor(colour.getBukkitColor());
		if (hasFade)
			for (Color colour : fade.getArray(e))
				builder.withFade(colour.getBukkitColor());
		
		builder.flicker(flicker);
		builder.trail(trail);
		return CollectionUtils.array(builder.build());
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "Firework effect " + type.toString(e, debug) + " with color " + color.toString(e, debug);
	}	
	
}