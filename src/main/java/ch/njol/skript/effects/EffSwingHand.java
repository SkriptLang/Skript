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
package ch.njol.skript.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Swing Hand")
@Description("Makes an entity swing their hand. This does nothing if the entity does not have an animation for swinging their hand.")
@Examples("make player swing their main hand")
@Since("2.5.1")
@RequiredPlugins("Minecraft 1.15.2+")
public class EffSwingHand extends Effect {
	
	static {
		Skript.registerEffect(EffSwingHand.class,
			"make %livingentities% swing [their] [main] hand",
			"make %livingentities% swing [their] off[ ]hand");
	}
	
	public static final boolean SWINGING_IS_SUPPORTED = Skript.methodExists(LivingEntity.class, "swingMainHand");
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	private boolean isMainHand;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!SWINGING_IS_SUPPORTED) {
			Skript.error("The swing hand effect requires Minecraft 1.15.2 or newer");
			return false;
		}
		entities = (Expression<LivingEntity>) exprs[0];
		isMainHand = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event e) {
		if (isMainHand) {
			for (LivingEntity entity : entities.getArray(e)) {
				entity.swingMainHand();
			}
		} else {
			for (LivingEntity entity : entities.getArray(e)) {
				entity.swingOffHand();
			}
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + entities.toString(e, debug) + " swing their " + (isMainHand ? "hand" : "off hand");
	}
	
}
