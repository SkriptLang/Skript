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
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.eclipse.jdt.annotation.Nullable;

@Name("Explosion Cause")
@Description("Get detonator/explosion cause in 'on explode' event")
@Examples({"on explode:",
	"\texplosion cause is a block",
	"\texplosion cause is an entity",
	"\tbroadcast \"%detonator% detonated an explosion at %event-location%\""})
@Events("explode")
@Since("INSERT VERSION")
public class ExprExplosionCause extends SimpleExpression<Object> {

	private static final int ENTITY = 1, BLOCK = 2, ANY = ENTITY | BLOCK;
	private int type;
	
	static {
		Skript.registerExpression(ExprExplosionCause.class, Object.class, ExpressionType.SIMPLE, "(explosion cause|detonator)");
	}
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BlockExplodeEvent.class, EntityExplodeEvent.class)) {
			Skript.error("Explosion cause can only be retrieved from explode event.");
			return false;
		}

		if (getParser().isCurrentEvent(BlockExplodeEvent.class) && getParser().isCurrentEvent(EntityExplodeEvent.class))
			type = ANY;
		else if (getParser().isCurrentEvent(BlockExplodeEvent.class))
			type = BLOCK;
		else if (getParser().isCurrentEvent(EntityExplodeEvent.class))
			type = ENTITY;

		return true;
	}

	// Attempting to fix an issue when using `on explode` without specifications and getting name of `explosion cause`
	// returning <none> because it returns Object.class and ExprName parses it as OfflinePlayer
	@Override
	protected @Nullable <R> ConvertedExpression<Object, ? extends R> getConvertedExpr(Class<R>... to) {
		for (Class c : to) {
			ConverterInfo entityConv = Converters.getConverterInfo(Entity.class, c);
			if (entityConv != null)
				return new ConvertedExpression(this, c, entityConv);
			ConverterInfo blockConv = Converters.getConverterInfo(Block.class, c);
			if (blockConv != null)
				return new ConvertedExpression(this, c, blockConv);
		}
		return null;
	}

	@Nullable
	@Override
	protected Object[] get(Event e) {
		if (e instanceof EntityExplodeEvent) {
			return new Entity[]{((EntityExplodeEvent) e).getEntity()};
		} else {
			return new Block[]{((BlockExplodeEvent) e).getBlock()};
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		if (type == BLOCK)
			return Block.class;
		else if (type == ENTITY)
			return Entity.class;
		else
			return Object.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "explosion cause";
	}
	
}
