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
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fishing Hooked Entity")
@Description("Returns the hooked entity of the fishing hook.")
@Examples({"on fish:",
			"\tif hooked entity is a player:",
			"\t\tteleport hooked entity to player"})
@Since("INSERT VERSION")
public class ExprFishingHookEntity extends SimpleExpression<Entity> {

	static {
		Skript.registerExpression(ExprFishingHookEntity.class, Entity.class, ExpressionType.SIMPLE, // This sometimes is different from getCaught from fish event - useful when pulling entities
			"hook[ed] entity [of [fish[ing]] hook]",
			"[fish[ing]] (hook'[s] [hooked]|hooked) entity");
	}

	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
			Skript.error("The 'fishing hooked entity' expression can only be used in fish event.");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable Entity[] get(Event e) {
		return new Entity[]{ ((PlayerFishEvent) e).getHook().getHookedEntity() };
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case DELETE:
			case SET:
				return CollectionUtils.array(Entity.class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null || delta[0] == null) {
			return;
		}

		FishHook hook = ((PlayerFishEvent) e).getHook();
		switch (mode) {
			case SET:
				hook.setHookedEntity((Entity) delta[0]);
				break;
			case DELETE:
				if (hook.getHookedEntity() != null && !(hook.getHookedEntity() instanceof Player))
					hook.getHookedEntity().remove();
				break;
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "hooked entity of fishing hook";
	}
}
