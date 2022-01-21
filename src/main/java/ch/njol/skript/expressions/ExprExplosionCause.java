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
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
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

	static {
		Skript.registerExpression(ExprExplosionCause.class, Object.class, ExpressionType.SIMPLE, "(explosion cause|detonator)");
	}

	private Class<?> returnType;
	private Class<?>[] returnTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BlockExplodeEvent.class, EntityExplodeEvent.class)) {
			Skript.error("Explosion cause can only be retrieved from explode event");
			return false;
		}

		if (getParser().isCurrentEvent(BlockExplodeEvent.class) && getParser().isCurrentEvent(EntityExplodeEvent.class))
			returnType = Object.class;
		else if (getParser().isCurrentEvent(BlockExplodeEvent.class))
			returnType = Block.class;
		else if (getParser().isCurrentEvent(EntityExplodeEvent.class))
			returnType = Entity.class;
		returnTypes = new Class[] {returnType};

		return true;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public @Nullable <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
		if (CollectionUtils.containsSuperclass(to, getReturnType()))
			return (Expression<? extends R>) this;

		boolean b = false;
		for (Class<?> c : to) {
			ConverterInfo entityConv = Converters.getConverterInfo(Entity.class, c);
			ConverterInfo blockConv = Converters.getConverterInfo(Block.class, c);
			if (entityConv != null || blockConv != null) {
				b = true;
				break;
			}
		}
		if (!b) {
			return null;
		}

		ExprExplosionCause exprExplosionCause = new ExprExplosionCause();
		exprExplosionCause.returnTypes = to;
		exprExplosionCause.returnType = Utils.getSuperType(to);
		return (Expression<? extends R>) exprExplosionCause;
	}

	@Override
	@Nullable
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Object[] get(Event e) {
		Object value = e instanceof EntityExplodeEvent ? ((EntityExplodeEvent) e).getEntity() : ((BlockExplodeEvent) e).getBlock();
		return Converters.convertArray(new Object[] {value}, returnTypes, (Class) returnType);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "explosion cause";
	}
	
}
