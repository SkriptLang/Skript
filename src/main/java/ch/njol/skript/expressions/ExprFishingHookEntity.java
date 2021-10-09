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

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Fishing Hooked Entity")
@Description("Returns the hooked entity of the fishing hook.")
@Examples({"on fish:",
			"\tif hooked entity of fishing hook is a player:",
			"\t\tteleport hooked entity of fishing hook to player"})
@Since("INSERT VERSION")
public class ExprFishingHookEntity extends SimplePropertyExpression<FishHook, Entity> {

	static {
		register(ExprFishingHookEntity.class, Entity.class, "hook[ed] entity", "fishinghook");
	}

	@Override
	@SuppressWarnings({"null", "unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<FishHook>) exprs[0]);
		return true;
	}

	@Override
	protected String getPropertyName() {
		return "hooked entity of fishing hook";
	}

	@Override
	public @Nullable Entity convert(FishHook fishHook) {
		return fishHook.getHookedEntity();
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
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

		FishHook[] hooks = getExpr().getArray(e);
		switch (mode) {
			case SET:
				for (FishHook fh : hooks)
					fh.setHookedEntity((Entity) delta[0]);
				break;
			case DELETE:
				for (FishHook fh : hooks) {
					if (fh.getHookedEntity() != null && !(fh.getHookedEntity() instanceof Player))
						fh.getHookedEntity().remove();
				}
				break;
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "hooked entity of " + getExpr().toString(e, debug);
	}
}
