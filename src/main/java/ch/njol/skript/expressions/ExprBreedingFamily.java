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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Breeding Family")
@Description("Represents a family within a breeding event")
@Examples({
	"on breeding:",
	"\tsend \"When a %mother% and %father% love each other they make a %offspring%\" to breeder"
})
@Since("INSERT VERSION")
public class ExprBreedingFamily extends SimpleExpression<LivingEntity> {

	static {
		Skript.registerExpression(ExprBreedingFamily.class, LivingEntity.class, ExpressionType.SIMPLE,
			"[breed[ing]] mother",
			"[breed[ing]] father",
			"[breed[ing]] (offspring|child)",
			"breeder");
	}

	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityBreedEvent.class)) {
			Skript.error("This expression can only be used within a Entity Breed event.");
			return false;
		}
		pattern = matchedPattern;
		return true;
	}

	@Override
	protected @Nullable LivingEntity[] get(Event event) {
		if (!(event instanceof EntityBreedEvent))
			return new LivingEntity[0];
		EntityBreedEvent breedEvent = (EntityBreedEvent) event;
		if (pattern == 0) {
			return new LivingEntity[]{breedEvent.getMother()};
		} else if (pattern == 1) {
			return new LivingEntity[]{breedEvent.getFather()};
		} else if (pattern == 2) {
			return new LivingEntity[]{breedEvent.getEntity()};
		} else if (pattern == 3) {
			return new LivingEntity[]{breedEvent.getBreeder()};
		}
		return new LivingEntity[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "breeding family";
	}
}
